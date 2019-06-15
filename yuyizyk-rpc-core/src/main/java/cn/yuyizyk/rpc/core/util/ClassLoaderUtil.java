package cn.yuyizyk.rpc.core.util;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * 根据包名-进行类加载
 * 
 * @author yuyi
 *
 */
public class ClassLoaderUtil {
	private static Logger log = LoggerFactory.getLogger(ClassLoaderUtil.class);

	/**
	 * 扫描包路径下的所有class文件
	 *
	 * @param pkgName   包名
	 * @param jar       jar文件
	 * @param recursive
	 * @param classes   保存包路径下class的集合
	 */
	private static void findClassesByJar(String pkgName, JarFile jar, Action<Class<?>> action) {
		String pkgDir = pkgName.replace(".", "/");

		Enumeration<JarEntry> entry = jar.entries();

		JarEntry jarEntry;
		String name, className;
		Class<?> claze;
		while (entry.hasMoreElements()) {
			jarEntry = entry.nextElement();

			name = jarEntry.getName();
			if (name.charAt(0) == '/') {
				name = name.substring(1);
			}

			if (jarEntry.isDirectory() || !name.startsWith(pkgDir) || !name.endsWith(".class")) {
				// 非指定包路径， 非class文件
				continue;
			}

			// 去掉后面的".class", 将路径转为package格式
			className = name.substring(0, name.length() - 6);
			claze = loadClass(className.replace("/", "."));
			if (claze != null) {
				action.apply(claze);
			}
		}
	}

	/**
	 * 扫描包路径下的所有class文件
	 *
	 * @param pkgName   包名
	 * @param pkgPath   包对应的绝对地址
	 * @param recursive
	 * @param classes   保存包路径下class的集合
	 */
	private static void findClassesByFile(String pkgName, String pkgPath, Action<Class<?>> action, Boolean recursive) {
		File dir = new File(pkgPath);
		if (!dir.exists() || !dir.isDirectory()) {
			return;
		}

		// 过滤获取目录，or class文件
		File[] dirfiles = dir.listFiles(pathname -> pathname.isDirectory() || pathname.getName().endsWith("class"));

		if (dirfiles == null || dirfiles.length == 0) {
			return;
		}

		String className;
		Class<?> clz;
		for (File f : dirfiles) {
			if (f.isDirectory() && recursive) {
				findClassesByFile(pkgName + "." + f.getName(), pkgPath + "/" + f.getName(), action, recursive);
				continue;
			}

			// 获取类名，干掉 ".class" 后缀
			className = f.getName();
			if (!StringUtils.isEmpty(className) && className.endsWith(".class")) {
				className = className.substring(0, className.length() - 6);
				// 加载类
				clz = loadClass(pkgName + "." + className);
				if (clz != null) {
					action.apply(clz);
				}
			}
		}
	}

	public static Class<?> loadClass(String fullClzName) {
		try {
			return Thread.currentThread().getContextClassLoader().loadClass(fullClzName);
		} catch (ClassNotFoundException e) {
			log.error("load class error! clz: {}, e:{}", fullClzName, e);
		}
		return null;
	}

	/**
	 * 扫描包路径下所有的class文件
	 *
	 * @param pkg
	 * @return
	 */
	public static Set<Class<?>> getClzFromPkg(String pkg) {
		Set<Class<?>> classes = new LinkedHashSet<>();
		scanning(pkg, c -> classes.add(c));
		return classes;
	}

	public static interface Action<T> {
		void apply(T t);
	}

	/**
	 * 扫描包路径下所有的class文件,是否递归扫描
	 *
	 * @param pkg
	 * @return
	 */
	public static void scanning(String pkg, Boolean recursive, Action<Class<?>> action) {
		String pkgDirName = pkg.replace('.', '/');
		try {
			Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(pkgDirName);
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				String protocol = url.getProtocol();
				if ("file".equals(protocol)) {// 如果是以文件的形式保存在服务器上
					String filePath = URLDecoder.decode(url.getFile(), "UTF-8");// 获取包的物理路径
					findClassesByFile(pkg, filePath, action, recursive);
				} else if ("jar".equals(protocol)) {// 如果是jar包文件
					JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
					findClassesByJar(pkg, jar, action);
				}
			}
		} catch (IOException e) {
			log.error("异常", e);
		}
	}

	/**
	 * 扫描包路径下所有的class文件
	 *
	 * @param pkg
	 * @return
	 */
	public static void scanning(String pkg, Action<Class<?>> action) {
		scanning(pkg, true, action);
	}
}
