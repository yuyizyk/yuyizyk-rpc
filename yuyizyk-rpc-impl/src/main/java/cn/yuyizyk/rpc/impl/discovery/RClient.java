package cn.yuyizyk.rpc.impl.discovery;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import javax.annotation.PostConstruct;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.yuyizyk.rpc.impl.entity.RPCError;
import cn.yuyizyk.rpc.impl.entity.RPCError.RPCNotFindServiceError;
import cn.yuyizyk.rpc.impl.entity.RPCError.RPCTimeOutError;
import cn.yuyizyk.rpc.impl.entity.RpcRequest;
import cn.yuyizyk.rpc.impl.entity.RpcResponse;
import cn.yuyizyk.rpc.impl.util.DoubleEntity;
import cn.yuyizyk.rpc.impl.util.RpcDecoder;
import cn.yuyizyk.rpc.impl.util.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RClient implements DisposableBean {
	private final EventLoopGroup group = new NioEventLoopGroup();
	private final Map<String, RPCConversation> mapper = new ConcurrentHashMap<>();

	private static RClient _single;

	public static RClient single() {
		return _single;
	}

	@PostConstruct
	public void init() {
		_single = this;
	}

	public void destroy() {
		group.shutdownGracefully();
	}

	@Autowired
	private LoadBalancerClientRoute loadBalancerClientRoute;

	public class RPCConversation {
		@Setter
		@Getter
		@Accessors(chain = true)
		private RpcRequest request;
		@Getter
		@Accessors(chain = true)
		private RpcResponse response;
		@Getter
		@Setter
		@Accessors(chain = true)
		private long timeout = 3000;

		private Thread curr;

		void back(RpcResponse response) {
			this.response = response;
			LockSupport.unpark(curr);
		}

		public RpcResponse send() {
			ChannelFuture future = null;
			mapper.put(request.getId(), this);
			DoubleEntity<String, Integer> entity = loadBalancerClientRoute.choose(request.getServiceName());
			if (entity == null) {
				response = new RpcResponse();
				response.setResult(new RPCNotFindServiceError(request.getServiceName()));
				return response;
			}
			try {
				future = pool.borrowObject(entity);
				ChannelFuture cf = future.channel().writeAndFlush(request).sync();
				cf.get(timeout, TimeUnit.MILLISECONDS);
				curr = Thread.currentThread();
				LockSupport.parkUntil(System.currentTimeMillis() + timeout);
				if (response == null) {
					response = new RpcResponse();
					response.setResult(new RPCTimeOutError());
					return response;
				}
				return response;
			} catch (Exception e) {
				log.error("", e);
				response = new RpcResponse();
				response.setResult(new RPCError().setCode(3).setError(e.getMessage()).setMsg(e.getLocalizedMessage()));
				return response;
			} finally {
				if (future != null)
					pool.returnObject(entity, future);
			}

		}

		public RPCConversationAsync async(Future<RpcResponse> callback) {
			return new RPCConversationAsync().setCallback(callback);
		}

		@Data
		@Accessors(chain = true)
		public class RPCConversationAsync {
			Future<RpcResponse> callback;

			public void send() {
				ChannelFuture future = null;
				DoubleEntity<String, Integer> entity = loadBalancerClientRoute.choose(request.getServiceName());
				try {
					future = pool.borrowObject(entity);
					future.channel().writeAndFlush(request).sync();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (future != null)
						pool.returnObject(entity, future);
				}
			}
		}
	}

	public RPCConversation create() {
		return new RPCConversation();
	}

	private final GenericKeyedObjectPool<DoubleEntity<String, Integer>, ChannelFuture> pool = new GenericKeyedObjectPool<>(
			new ClientKeyedPooledObjectFactory(), new GenericKeyedObjectPoolConfig<ChannelFuture>() {
				{
					setMaxWaitMillis(3000);// 空闲获取超时时间
					setSoftMinEvictableIdleTimeMillis(1000 * 60 * 30L);// 对象最小的空间时间 在超出最小空闲数量(minIdle)时进行移除超时连接
					setTestOnReturn(false);
					// setMinIdle(1);
					setMinIdlePerKey(1);// 子池最小空闲
					setLifo(true);
					setMaxIdlePerKey(2);// 子池最大
					/** 支持jmx管理扩展 */
					setJmxEnabled(true);
					setJmxNamePrefix("ClientPoolProtocol");
					/** 保证获取有效的池对象 */
					setTestOnBorrow(true);
					setTestOnReturn(true);
				}
			});

	private final class ClientKeyedPooledObjectFactory
			extends BaseKeyedPooledObjectFactory<DoubleEntity<String, Integer>, ChannelFuture> {
		@Override
		public ChannelFuture create(DoubleEntity<String, Integer> key) throws Exception {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel channel) throws Exception {
					channel.pipeline().addLast(new RpcEncoder()) // 将 RPC 请求进行编码（为了发送请求）
							.addLast(new RpcDecoder()) // 将 RPC 响应进行解码（为了处理响应）
							.addLast(new CChannelInokerHandler()); // 发送 RPC 请求
				}
			}).option(ChannelOption.SO_KEEPALIVE, true);
			ChannelFuture future = bootstrap.connect(key.getEntity1(), key.getEntity2()).sync();
			return future;
		}

		@Override
		public PooledObject<ChannelFuture> wrap(ChannelFuture value) {
			return new DefaultPooledObject<ChannelFuture>(value);
		}

	}

	private final class CChannelInokerHandler extends SimpleChannelInboundHandler<RpcResponse> {
		@Override
		protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
			mapper.get(response.getId()).back(response);
		}
	}
}
