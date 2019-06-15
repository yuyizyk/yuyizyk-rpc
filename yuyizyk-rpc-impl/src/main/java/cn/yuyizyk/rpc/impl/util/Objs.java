package cn.yuyizyk.rpc.impl.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object Utils
 * 
 * <p>
 * 参考：
 * <ul>
 * <li>https://github.com/looly/hutool/tree/v4-master/hutool-core/src/main/java/cn/hutool/core/util</li>
 * </ul>
 * </p>
 *
 * @author yuyi
 */
public class Objs {
	private final static Logger log = LoggerFactory.getLogger(Objs.class);

	public static final boolean eq(Object obj1, Object obj2) {
		return obj1 == obj2 || obj1 != null && obj1.equals(obj2);
	}

	public static final String toString(Object obj) {
		if (Objs.isEmpty(obj))
			return null;
		return obj.toString();
	}

	/**
	 * 深拷贝
	 * 
	 * @param src
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static <T> T cloneByStream(T src) {
		if (Objects.isNull(src) || !(src instanceof Serializable)) {
			return null;
		}
		final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		try (ObjectOutputStream out = new ObjectOutputStream(byteOut)) {
			out.writeObject(src);

			ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
			ObjectInputStream in = new ObjectInputStream(byteIn);
			@SuppressWarnings("unchecked")
			T dest = (T) in.readObject();
			return dest;
		} catch (IOException | ClassNotFoundException e) {
			log.error(" cloneByStream error ", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * <pre>
	 * isEmpty(null)                        = true
	 * isEmpty(new Object(),null)           = true
	 * isEmpty(new Object(),new Object())   = false
	 * isEmpty(new Object(),[null])         = true
	 * </pre>
	 * 
	 * @param args
	 * @return
	 */
	public static final boolean anyEmpty(final Object arg, final Object... args) {
		return isEmpty(arg) || anyEmpty(args);
	}

	/**
	 * <pre>
	 * isEmpty(null)      = true
	 * isEmpty([])        = true
	 * isEmpty([null])    = true
	 * </pre>
	 * 
	 * @param args
	 * @return
	 */
	public static final boolean isEmpty(final Object[] args) {
		if (args == null || args.length == 0)
			return true;
		// for (int i = 0; i < args.length; i++) {
		// if (isEmpty(args[i]))
		// return true;
		// }
		return false;
	}

	public static final boolean anyEmpty(final Object[] args) {
		if (isEmpty(args))
			return true;
		for (int i = 0; i < args.length; i++) {
			if (isEmpty(args[i]))
				return true;
		}
		return false;
	}

	/**
	 * <pre>
	 * isEmpty(null)      = true
	 * isEmpty([])        = true
	 * isEmpty([null])    = false
	 * </pre>
	 * 
	 * @param args
	 * @return
	 */
	public static final boolean isEmpty(final Collection<?> args) {
		return args == null || args.isEmpty();
	}

	/**
	 * <pre>
	 * isEmpty(null)      = true
	 * isEmpty({})        = true
	 * isEmpty({null=null})       = false
	 * </pre>
	 * 
	 * @param args
	 * @return
	 */
	public static final boolean isEmpty(final Map<?, ?> args) {
		return args == null || args.isEmpty();
	}

	/**
	 * 
	 * @param args
	 * @return args == null
	 */
	public static final boolean isEmpty(final Object args) {
		return Objects.isNull(args);
	}

	/**
	 * <pre>
	 * isEmpty(null)      = true
	 * isEmpty("")        = true
	 * isEmpty(" ")       = false
	 * isEmpty("bob")     = false
	 * isEmpty("  bob  ") = false
	 * </pre>
	 * 
	 * @param args
	 * @return
	 */
	public static final boolean isEmpty(final CharSequence cs) {
		return StringUtils.isEmpty(cs);
	}

	/**
	 * 数组是否为空
	 * 
	 * @param array 数组
	 * @return 是否为空
	 */
	public static boolean isEmpty(final long... array) {
		return array == null || array.length == 0;
	}

	/**
	 * 数组是否为空
	 * 
	 * @param array 数组
	 * @return 是否为空
	 */
	public static boolean isEmpty(final int... array) {
		return array == null || array.length == 0;
	}

	/**
	 * 数组是否为空
	 * 
	 * @param array 数组
	 * @return 是否为空
	 */
	public static boolean isEmpty(final short... array) {
		return array == null || array.length == 0;
	}

	/**
	 * 数组是否为空
	 * 
	 * @param array 数组
	 * @return 是否为空
	 */
	public static boolean isEmpty(final char... array) {
		return array == null || array.length == 0;
	}

	/**
	 * 数组是否为空
	 * 
	 * @param array 数组
	 * @return 是否为空
	 */
	public static boolean isEmpty(final byte... array) {
		return array == null || array.length == 0;
	}

	/**
	 * 数组是否为空
	 * 
	 * @param array 数组
	 * @return 是否为空
	 */
	public static boolean isEmpty(final double... array) {
		return array == null || array.length == 0;
	}

	/**
	 * 数组是否为空
	 * 
	 * @param array 数组
	 * @return 是否为空
	 */
	public static boolean isEmpty(final float... array) {
		return array == null || array.length == 0;
	}

	/**
	 * 数组是否为空
	 * 
	 * @param array 数组
	 * @return 是否为空
	 */
	public static boolean isEmpty(final boolean... array) {
		return array == null || array.length == 0;
	}

	/**
	 * 包含{@code null}元素，或 数组为空{@code null}
	 * 
	 * @param array
	 * @return
	 */
	public static boolean anyNull(Object... array) {
		return isEmpty(array);
	}

	/**
	 * 检查是否为有效的数字<br>
	 * 检查Double和Float是否为无限大，或者Not a Number<br>
	 * 非数字类型和Null将返回true
	 * 
	 * @param obj 被检查类型
	 * @return 检查结果，非数字类型和Null将返回true
	 */
	public static boolean isValidIfNumber(Object obj) {
		if (obj != null && obj instanceof Number) {
			if (((Double) obj).isInfinite() || ((Double) obj).isNaN()) {
				if (obj instanceof Double) {
					return false;
				}
			} else if (obj instanceof Float) {
				if (((Float) obj).isInfinite() || ((Float) obj).isNaN()) {
					return false;
				}
			}
		}
		return true;
	}

	public static final byte[] toBytes(String bytes) {
		String[] strs = bytes.replaceAll("[", "").replaceAll(" ", "").replaceAll("]", "").split(",");
		byte[] bs = new byte[strs.length];
		for (int i = 0; i < strs.length; i++) {
			bs[i] = Byte.parseByte(strs[i]);
		}
		return bs;
	}

}
