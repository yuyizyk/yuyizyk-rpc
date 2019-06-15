package cn.yuyizyk.rpc.core;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;

@Order(RPCInitializer.rpcInitializer_ORDER_Index)
public class RPCInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	public static final int rpcInitializer_ORDER_Index = 0;

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		
	}

}
