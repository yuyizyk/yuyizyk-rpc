package cn.yuyizyk.rpc.impl.discovery;

import java.util.List;
import java.util.Map.Entry;

import org.springframework.beans.factory.FactoryBean;

import cn.yuyizyk.rpc.core.util.RPCUtils.ServiceAnalysis;

/**
 * 
 * 
 * @author yuyi
 * @param <T>
 */
public class RPCClientFactoryBean<T> implements FactoryBean<T> {
	private final Entry<Class<?>, List<ServiceAnalysis>> entry;

	public RPCClientFactoryBean(final Entry<Class<?>, List<ServiceAnalysis>> entry) {
		this.entry = entry;
	}

	@Override
	public T getObject() throws Exception {
		T inter = RPCProxy.create(entry);
		return inter;
	}

	public boolean isSingleton() {
		return true;
	}

	@Override
	public Class<?> getObjectType() {
		return entry.getKey();
	}

}
