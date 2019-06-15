package cn.yuyizyk.rpc.core.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.util.StringUtils;

import cn.yuyizyk.rpc.core.anno.RService;
import lombok.Data;
import lombok.experimental.Accessors;

public class RPCUtils {

	private static final RPCUtils runParameter = new RPCUtils();

	public static RPCUtils single() {
		return runParameter;
	}

	private final List<String> backages = new CopyOnWriteArrayList<>();

	public static void addRPCServiceBackages(String backage) {
		single().backages.add(backage);
		ClassLoaderUtil.getClzFromPkg(backage).forEach(c -> {
			if (c.isAnnotationPresent(RService.class) && ((c.getModifiers() & Modifier.PUBLIC) == 1)) {
				single().rpcServiceMaps.put(c, analysisToService(c));
			}
		});
	}

	private final Map<Class<?>, List<ServiceAnalysis>> rpcServiceMaps = new ConcurrentHashMap<>();

	public static Map<Class<?>, List<ServiceAnalysis>> getRPCServiceMaps() {
		return single().rpcServiceMaps;
	}

	@Data
	@Accessors(chain = true)
	public static class ServiceAnalysis {
		private String name;
		private Class<?> clz;
		private Method m;
	}

	public static boolean isRPCService(Class<?> clz) {
		return clz != null && single().rpcServiceMaps.keySet().stream().anyMatch(c -> c.isAssignableFrom(clz));
	}

	public static List<ServiceAnalysis> getRPCServiceByCache(Class<?> clz) {
		List<ServiceAnalysis> list = new ArrayList<>();
		single().rpcServiceMaps.entrySet().stream().filter(c -> c.getKey().isAssignableFrom(clz))
				.forEach(e -> list.addAll(e.getValue()));
		return list;
	}

	/**
	 * 解析 RPCService DEFINDE
	 * 
	 * @param clz
	 * @return
	 */
	public static List<ServiceAnalysis> analysisToService(Class<?> clz) {
		RService server = clz.getAnnotation(RService.class);
		if (server == null)
			return null;
		List<ServiceAnalysis> list = new ArrayList<>();
		RService tempM;
		String name;
		String clzName = server.value();
		if (StringUtils.isEmpty(clzName)) {
			clzName = clz.getName();
		}
		for (Method m : clz.getMethods()) {
			if (m.getDeclaringClass().isAnnotationPresent(RService.class) && !m.isBridge()) {
				tempM = m.getAnnotation(RService.class);
				if (tempM != null && !StringUtils.isEmpty(tempM.value())) {
					name = tempM.value();
				} else {
					name = clzName + "::" + m.getName();// 不考虑多态。避免名字过长
				}
				list.add(new ServiceAnalysis().setClz(clz).setM(m).setName(name));
			}
		}
		return list;
	}

}
