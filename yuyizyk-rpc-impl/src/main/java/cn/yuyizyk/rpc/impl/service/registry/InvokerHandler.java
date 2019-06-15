package cn.yuyizyk.rpc.impl.service.registry;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.stereotype.Component;

import cn.yuyizyk.rpc.impl.entity.RPCError;
import cn.yuyizyk.rpc.impl.entity.RPCError.RPCNotFindServiceError;
import cn.yuyizyk.rpc.impl.entity.RpcRequest;
import cn.yuyizyk.rpc.impl.entity.RpcResponse;
import cn.yuyizyk.rpc.impl.service.filter.RServerAfterFilter;
import cn.yuyizyk.rpc.impl.service.filter.RServerFilter;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 处理请求
 * 
 * 
 * {@link https://blog.csdn.net/qq924862077/article/details/52946617}
 *
 * @author yuyi
 */
@Slf4j
@Sharable
@ConditionalOnExpression("#{ '${rpc.close-server:false}' == 'false' }")
@Component
public class InvokerHandler extends SimpleChannelInboundHandler<RpcRequest> {

	private final List<RServerAfterFilter> rServerAfterFilters = new ArrayList<>();

	@Autowired(required = false)
	public void setFilters(List<RServerFilter> filters) {
		filters.forEach(this::addFilter);
	}

	@Autowired
	private SpringServiceFinder finder;

	public InvokerHandler addFilter(RServerFilter filter) {
		if (RServerAfterFilter.class.isAssignableFrom(filter.getClass())) {
			log.info("find RServerAfterFilter filter {} ", filter.getClass().getName());
			rServerAfterFilters.add((RServerAfterFilter) filter);
		}
		return this;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error("", cause);
		ctx.close();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
		log.debug(" RpcRequest : {} ", request);
		RpcResponse response = new RpcResponse();
		response.setId(request.getId());
		response.setResult(handle(request));
		log.debug(" RpcResponse : {} ", response);
		ctx.writeAndFlush(response)/* .addListener(ChannelFutureListener.CLOSE) */;
		for (RServerAfterFilter rServerAfterFilter : rServerAfterFilters)
			rServerAfterFilter.after();
	}

	private Object handle(RpcRequest request) {
		Object serviceBean = finder.getObj(request.getServiceName());
		if (serviceBean == null) {
			return new RPCNotFindServiceError(request.getServiceName());
		}
		Class<?> serviceClass = serviceBean.getClass();
		String methodName = request.getMethodName();
		Class<?>[] parameterTypes = request.getParameterTypes();
		Object[] parameters = request.getParameters();
		try {
			/*
			 * Method method = serviceClass.getMethod(methodName, parameterTypes);
			 * method.setAccessible(true); return method.invoke(serviceBean, parameters);
			 */

			FastClass serviceFastClass = FastClass.create(serviceClass);
			FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
			log.debug(" Clz:{} ,Method:{} ,Par:{} ", serviceClass, methodName, parameters);
			return serviceFastMethod.invoke(serviceBean, parameters);
		} catch (Throwable e) {
			log.error("", e);
			return new RPCError().setCode(2).setMsg(e.getLocalizedMessage()).setError(e.getMessage());
		}
	}
}
