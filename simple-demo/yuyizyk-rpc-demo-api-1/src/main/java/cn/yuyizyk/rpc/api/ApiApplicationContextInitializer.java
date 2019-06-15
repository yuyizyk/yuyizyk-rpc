package cn.yuyizyk.rpc.api;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;

import cn.yuyizyk.rpc.core.RPCInitializer;
import cn.yuyizyk.rpc.core.util.RPCUtils;

@Order(RPCInitializer.rpcInitializer_ORDER_Index - 100)
public class ApiApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		RPCUtils.addRPCServiceBackages(this.getClass().getPackage().getName());
	}
}
