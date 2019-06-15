package cn.yuyizyk.rpc.impl.util;

import java.sql.Timestamp;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class RpcDecoder extends ByteToMessageDecoder {
	private final ProtostuffSerializeUtil protostuffSerializeUtil = new ProtostuffSerializeUtil();

	@Override
	public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (in.readableBytes() < 4) {// tcp沾包
			return;
		}
		in.markReaderIndex();
		int dataLength = in.readInt();
		if (dataLength < 0) {
			ctx.close();
		}
		if (in.readableBytes() < dataLength) {
			in.resetReaderIndex();
			return;
		}
		byte[] data = new byte[dataLength];
		in.readBytes(data);
		Object obj = protostuffSerializeUtil.unserialize(data);
		out.add(obj);
	}

	public static void main(String[] args) {
		byte[] data = new ProtostuffSerializeUtil().serialize(new Timestamp(System.currentTimeMillis()));

		System.out.println(((Object) new ProtostuffSerializeUtil().unserialize(data)));

	}

}
