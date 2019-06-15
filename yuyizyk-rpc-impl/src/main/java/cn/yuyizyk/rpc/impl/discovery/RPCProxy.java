package cn.yuyizyk.rpc.impl.discovery;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import cn.yuyizyk.rpc.core.anno.RService;
import cn.yuyizyk.rpc.core.util.RPCUtils;
import cn.yuyizyk.rpc.core.util.RPCUtils.ServiceAnalysis;
import cn.yuyizyk.rpc.impl.entity.RpcRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * 生成客户端代理对象
 * 
 * 
 *
 * @author yuyi
 */
@Slf4j
public class RPCProxy {

	/**
	 * @param clz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T create(Class<?> clz) {
		if (!RPCUtils.getRPCServiceMaps().containsKey(clz)) {
			return null;
		}

		final RService rService = clz.getAnnotation(RService.class);
		String name = rService.value();
		if (StringUtils.isEmpty(name)) {
			name = clz.getName();
		}
		final String clzName = name;
		return (T) Proxy.newProxyInstance(clz.getClassLoader(), new Class<?>[] { clz }, new InvocationHandler() {
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				if (!method.getDeclaringClass().isAnnotationPresent(RService.class)) {
					return method.invoke(proxy, args);
				}
				String serviceName;
				RService tempM = method.getAnnotation(RService.class);
				if (tempM != null && StringUtils.isNotEmpty(tempM.value())) {
					serviceName = tempM.value();
				} else {
					serviceName = clzName + "::" + method.getName();// 不考虑多态。避免名字过长
				}

				RpcRequest request = new RpcRequest().setId(UUID.randomUUID().toString()).setServiceName(serviceName)
						.setClassName(clz.getName()).setMethodName(method.getName())
						.setParameterTypes(method.getParameterTypes()).setParameters(args);
				Object obj = RClient.single().create().setRequest(request).send().getResult();

				if (obj == null) {
					return null;
				}
				if (method.getReturnType().isInstance(obj)) {
					return obj;
				}
				log.error("REQ : {}, RESP BODY :{} ", request, obj);
				return null;
			}
		});

	}

	public static <T> T create(Entry<Class<?>, List<ServiceAnalysis>> entry) {
		if (!RPCUtils.getRPCServiceMaps().containsKey(entry.getKey())) {
			log.error("{} NOT IS RPCIService.", entry.getKey());
			return null;
		}
		return create(entry.getKey());
	}
}
