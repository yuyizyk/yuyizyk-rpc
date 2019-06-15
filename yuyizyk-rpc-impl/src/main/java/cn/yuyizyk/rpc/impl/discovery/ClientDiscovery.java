package cn.yuyizyk.rpc.impl.discovery;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;

import cn.yuyizyk.rpc.core.util.RPCUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * RPC 客户端
 * 
 *
 * @author yuyi
 */
@Slf4j
public class ClientDiscovery implements InstantiationAwareBeanPostProcessor {

	public void setConfigurableApplicationContext(ConfigurableApplicationContext applicationContext) {
		ConfigurableListableBeanFactory f = applicationContext.getBeanFactory();
		f.addBeanPostProcessor(this);
		DefaultListableBeanFactory bf = (DefaultListableBeanFactory) (f);
		RPCUtils.getRPCServiceMaps().entrySet().forEach(e -> {
			GenericBeanDefinition definition = new GenericBeanDefinition();
			ConstructorArgumentValues constr = new ConstructorArgumentValues();
			constr.addGenericArgumentValue(e);
			definition.setConstructorArgumentValues(constr);
			definition.setBeanClass(RPCClientFactoryBean.class);
			definition.setScope("singleton"); // 设置scope
			definition.setLazyInit(true); // 设置是否懒加载
			definition.setAutowireCandidate(true); // 设置是否可以被其他对象自动注入
			String beanName = new StringBuilder().append(Character.toLowerCase(e.getKey().getSimpleName().charAt(0)))
					.append(e.getKey().getSimpleName().substring(1)).toString();
			bf.registerBeanDefinition(beanName, definition);
			log.debug("setBean {} ServiceNames:{}", e.getKey(), e.getValue());
		});
		log.info("set RPCServiceBean Client size {}", RPCUtils.getRPCServiceMaps().size());
	}
}
