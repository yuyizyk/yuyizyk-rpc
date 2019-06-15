package cn.yuyizyk.rpc.impl.util;

import java.io.Serializable;

public class Box<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	private T obj;

	public Box() {
	}

	public Box(T obj) {
		setObj(obj);
	}

	public T getObj() {
		return obj;
	}

	public void setObj(T obj) {
		this.obj = obj;
	}
}
