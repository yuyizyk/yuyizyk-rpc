package cn.yuyizyk.rpc.impl.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.protostuff.Input;
import io.protostuff.LinkedBuffer;
import io.protostuff.Output;
import io.protostuff.Pipe;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.WireFormat.FieldType;
import io.protostuff.runtime.DefaultIdStrategy;
import io.protostuff.runtime.Delegate;
import io.protostuff.runtime.RuntimeEnv;
import io.protostuff.runtime.RuntimeSchema;

/**
 * 
 * Protostuff 序列化工具<br/>
 * 效率高
 * 
 *
 * @author yuyi
 */
public class ProtostuffSerializeUtil  {
	private final static Logger log = LoggerFactory.getLogger(ProtostuffSerializeUtil.class);
	private final static DefaultIdStrategy idStrategy = ((DefaultIdStrategy) RuntimeEnv.ID_STRATEGY);
	static {
		idStrategy.registerDelegate(new Delegate<Timestamp>() {
			public FieldType getFieldType() {
				return FieldType.FIXED64;
			}

			public Class<?> typeClass() {
				return Timestamp.class;
			}

			public Timestamp readFrom(Input input) throws IOException {
				return new Timestamp(input.readFixed64());
			}

			public void writeTo(Output output, int number, Timestamp value, boolean repeated) throws IOException {
				output.writeFixed64(number, value.getTime(), repeated);
			}

			public void transfer(Pipe pipe, Input input, Output output, int number, boolean repeated)
					throws IOException {
				output.writeFixed64(number, input.readFixed64(), repeated);
			}

		});
	}

	@SuppressWarnings("rawtypes")
	private final static Schema<Box> schema = RuntimeSchema.getSchema(Box.class, idStrategy);
	// public static void main(String[] args) {
	// List<String> b = new ArrayList<>();
	// b.add("123");
	// Box<List<String>> bb = new Box<>();
	// bb.setObj(b);
	// ProtostuffSerializeUtil u = new ProtostuffSerializeUtil();
	// String s = u.serializeToStr(bb);
	// System.out.println(s);
	// System.out.println(JSONObject.toJSONString(bb = u.deserializeByStr(s,
	// Box.class)));
	// System.out.println(bb);
	//
	// }

	public void serialization(Object obj, OutputStream os) {
		if (obj == null || os == null)
			return;
		Box<Object> box = new Box<>();
		box.setObj(obj);

		try {
			ProtostuffIOUtil.writeTo(os, box, schema, LinkedBuffer.allocate(256));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public <T> T deserialize(InputStream is) {
		Box<T> box = new Box<>();

		try {
			ProtostuffIOUtil.mergeFrom(is, box, schema);
		} catch (IOException e) {
			log.error("异常 ", e);
		}
		return box.getObj();
	}

	public String serializeToStr(Object obj) {
		if (obj == null) {
			return null;
		}
		return Base64.getEncoder().encodeToString(serialize(obj));
	}

	public <T> byte[] serialize(T obj) {
		if (obj == null) {
			return null;
		}

		Box<T> box = new Box<>();
		box.setObj(obj);
		return ProtostuffIOUtil.toByteArray(box, schema, LinkedBuffer.allocate(256));
	}

	public <T> T unserialize(byte[] bytes, Class<T> clazz) {
		return unserialize(bytes);
	}

	public <T> T unserialize(byte[] bytes) {
		Box<T> box = new Box<>();
		ProtostuffIOUtil.mergeFrom(bytes, box, schema);
		return box.getObj();

	}

	public <T> T deserializeByStr(String obj) {
		if (obj == null)
			return null;
		return unserialize(Base64.getDecoder().decode(obj));
	}

	public <T> T deserializeByStr(byte[] data) {
		Box<T> box = new Box<>();
		ProtostuffIOUtil.mergeFrom(data, box, schema);
		return box.getObj();
	}

}
