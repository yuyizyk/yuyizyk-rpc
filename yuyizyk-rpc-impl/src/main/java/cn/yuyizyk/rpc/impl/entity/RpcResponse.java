package cn.yuyizyk.rpc.impl.entity;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import lombok.Data;

/**
 * 请求实体
 * 
 * 
 *
 * @author yuyi
 */
@Data
public class RpcResponse implements Serializable {
	private static final long serialVersionUID = 1L;
	/** */
	private String id;
	private Object result;

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("id", getId())
				.append("result", result).toString();
	}
}
