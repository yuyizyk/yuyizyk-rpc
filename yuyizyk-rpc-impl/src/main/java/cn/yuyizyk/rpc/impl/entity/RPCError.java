package cn.yuyizyk.rpc.impl.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RPCError {
	private String msg;
	private int code;
	private String error;

	@Data
	@EqualsAndHashCode(callSuper = false)
	@Accessors(chain = true)
	public static class RPCNotFindServiceError extends RPCError {
		private String serviceName;

		public RPCNotFindServiceError(String serviceName) {
			setCode(1);
			setMsg("没有获取到Service:" + serviceName);
			setServiceName(serviceName);
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("code", getCode())
					.append("error", getError()).append("msg", getMsg()).append("serviceName", getServiceName())
					.toString();
		}
	}

	public static class RPCTimeOutError extends RPCError {
		public RPCTimeOutError() {
			setCode(1);
			setMsg("服务请求超时");
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("code", code).append("error", error)
				.append("msg", msg).toString();
	}
}
