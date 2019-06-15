package cn.yuyizyk.rpc.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

import cn.yuyizyk.rpc.core.util.SpringContextUtils;

@Configuration
@Order
public class RPCCoreAutoConfiguration {
	@Autowired
	protected ConfigurableEnvironment environment;

	@Bean
	public SpringContextUtils springContextUtils() {
		return new SpringContextUtils();
	}

}
