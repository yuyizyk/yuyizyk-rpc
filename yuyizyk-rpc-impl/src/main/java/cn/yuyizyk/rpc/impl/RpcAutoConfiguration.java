package cn.yuyizyk.rpc.impl;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.consul.discovery.HeartbeatProperties;
import org.springframework.cloud.consul.discovery.TtlScheduler;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.ecwid.consul.v1.ConsulClient;

import cn.yuyizyk.rpc.core.RPCInitializer;
import cn.yuyizyk.rpc.impl.discovery.ClientDiscovery;
import cn.yuyizyk.rpc.impl.service.registry.RpcServer;
import cn.yuyizyk.rpc.impl.service.registry.SpringServiceFinder;

@Configuration
@ConditionalOnClass({ RpcServer.class, ClientDiscovery.class })
// @AutoConfigureAfter()
@ComponentScan({ "cn.yuyizyk.rpc.impl" })
@ConditionalOnProperty(prefix = "rpc", value = "enabled", matchIfMissing = true)
@Order(RPCInitializer.rpcInitializer_ORDER_Index + 1000)
public class RpcAutoConfiguration implements ApplicationContextInitializer<ConfigurableApplicationContext> {
	private static ClientDiscovery clientDiscovery = new ClientDiscovery();
	private static ConfigurableListableBeanFactory f;

	private static SpringServiceFinder springServiceFinder = new SpringServiceFinder();

	@Bean
	@ConditionalOnExpression("#{ '${rpc.close-client:false}' == 'false' }")
	public ClientDiscovery clientDiscovery() {
		return clientDiscovery;
	}

	@Bean
	@ConditionalOnExpression("#{ '${rpc.close-server:false}' == 'false' }")
	public SpringServiceFinder SpringServiceFinder() {
		return springServiceFinder;
	}

	@Bean
	@ConditionalOnExpression("#{ '${rpc.close-server:false}' == 'false' }")
	@ConditionalOnMissingBean
	public TtlScheduler ttlScheduler(ConsulClient consulClient, HeartbeatProperties heartbeatProperties) {
		return new TtlScheduler(heartbeatProperties, consulClient);
	}

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		f = applicationContext.getBeanFactory();
		clientDiscovery.setConfigurableApplicationContext(applicationContext);
		f.addBeanPostProcessor(springServiceFinder);
	}
}
