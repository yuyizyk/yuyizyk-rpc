package cn.yuyizyk.rpc.impl.service.registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;

import cn.yuyizyk.rpc.core.util.RPCUtils;
import cn.yuyizyk.rpc.core.util.RPCUtils.ServiceAnalysis;
import cn.yuyizyk.rpc.impl.util.DoubleEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * 将所有服务查找
 * 
 *
 * @author yuyi
 */
@Slf4j
@Data
public class SpringServiceFinder implements InstantiationAwareBeanPostProcessor {

	private final Map<String, DoubleEntity<ServiceAnalysis, Object>> handlerMap = new HashMap<>();

	/**
	 * 将注册到spring 容器中的服务注册到rpc服务中 <br/>
	 */
	@Override
	public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {

		if (RPCUtils.isRPCService(bean.getClass())) {
			List<ServiceAnalysis> s = RPCUtils.getRPCServiceByCache(bean.getClass());
			s.stream().forEach(serviceAnalysis -> {
				if (handlerMap.containsKey(serviceAnalysis.getName())) {
					log.error("{} 重复", serviceAnalysis.getName());
					return;
				}
				handlerMap.put(serviceAnalysis.getName(), DoubleEntity.builder(serviceAnalysis, bean));
				log.debug(" find RPC serivce ：{}  is Bean :{}", serviceAnalysis.getName(), beanName);
			});
		}
		return InstantiationAwareBeanPostProcessor.super.postProcessAfterInstantiation(bean, beanName);
	}

	/**
	 * 获得spring 容器 中的服务
	 * 
	 * @param serverName
	 * @param clzName
	 * @return
	 */
	public DoubleEntity<ServiceAnalysis, Object> get(String sName) {
		return handlerMap.get(sName);
	}

	/**
	 * 获得spring 容器 中的服务
	 * 
	 * @param serverName
	 * @param clzName
	 * @return
	 */
	public Object getObj(String sName) {
		return handlerMap.getOrDefault(sName, DoubleEntity.builder(null, null)).getEntity2();
	}

}
