package cn.yuyizyk.rpc.impl.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcEncoder extends MessageToByteEncoder<Object> {
	private final ProtostuffSerializeUtil protostuffSerializeUtil = new ProtostuffSerializeUtil();

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		byte[] data = protostuffSerializeUtil.serialize(msg);
		out.writeInt(data.length);
		out.writeBytes(data);
	}

}
