package cn.yuyizyk.rpc.impl.entity;

import java.io.Serializable;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import cn.yuyizyk.rpc.impl.util.Box;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 请求实体
 * 
 * 
 *
 * @author yuyi
 */
@Data
@Accessors(chain = true)
public class RpcRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	/** uuid */
	private String id;
	/** 服务名 */
	private String serviceName;

	private String className;
	/** 函数名称 */
	private String methodName;
	/** 参数类型 */
	private Class<?>[] parameterTypes;
	/** 参数列表 */
	private Box<Object>[] parameters;

	public Object[] getParameters() {
		return parameters == null ? null : Stream.of(parameters).map(Box::getObj).toArray(Object[]::new);
	}

	@SuppressWarnings("unchecked")
	public RpcRequest setParameters(Object[] parameters) {
		this.parameters = Stream.of(parameters).map(Box::new).toArray(Box[]::new);
		return this;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("c", getClassName())
				.append("m", getMethodName()).append("id", getId()).toString();
	}
}
