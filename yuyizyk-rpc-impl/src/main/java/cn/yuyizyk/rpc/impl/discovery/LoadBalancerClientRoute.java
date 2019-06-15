package cn.yuyizyk.rpc.impl.discovery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Component;

import cn.yuyizyk.rpc.impl.util.DoubleEntity;

@Component
public class LoadBalancerClientRoute {

	@Autowired
	private LoadBalancerClient loadBalancerClient;

	protected DoubleEntity<String, Integer> choose(String serviceId) {
		ServiceInstance instance = loadBalancerClient.choose(serviceId);
		if(instance==null) {
			return null;
		}
		return DoubleEntity.builder(instance.getHost(), instance.getPort());
	}

}
