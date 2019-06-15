package cn.yuyizyk.rpc.impl.service.registry;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import cn.yuyizyk.rpc.impl.util.RpcDecoder;
import cn.yuyizyk.rpc.impl.util.RpcEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 服务器注册
 * 
 * 
 *
 * @author yuyi
 */
@Slf4j
@ConditionalOnExpression("#{ '${rpc.close-server:false}' == 'false' }")
@Component
public class RpcServer implements DisposableBean, ApplicationRunner {
	@Value("${rpc.static-port:${random.int[7000,7999]}}")
	private Integer port;

	public Integer getServerPort() {
		return port;
	}

	public boolean isDestroy() {
		return isdestroy;
	}

	/** 处理器 */
	@Autowired
	private InvokerHandler invokerHandler;
	private final EventLoopGroup bossGroup = new NioEventLoopGroup();
	private final EventLoopGroup workerGroup = new NioEventLoopGroup();
	private boolean isdestroy = false;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		try {
			ServerBootstrap bootstrap = new ServerBootstrap().group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class).localAddress(port)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline pipeline = ch.pipeline();
							pipeline.addLast(new RpcDecoder()).addLast(new RpcEncoder())
									// pipeline.addLast("encoder", new ObjectEncoder());
									// pipeline.addLast("decoder",
									// new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
									.addLast(invokerHandler);
						}
					}).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);
			ChannelFuture future = bootstrap.bind(port).sync();
			log.info(" 注册服务成功.PORT:{} ", port);
			future.channel().closeFuture().sync();

		} catch (InterruptedException e) {
			log.error("注册失败", e);
			destroy();
		}
	}

	@Override
	public void destroy() throws Exception {
		if (isdestroy) {
			return;
		}
		log.info("destroy RPC NETTY SERVER...");
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
		isdestroy = true;
	}

}
