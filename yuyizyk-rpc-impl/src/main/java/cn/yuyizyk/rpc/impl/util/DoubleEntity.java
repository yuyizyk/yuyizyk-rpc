package cn.yuyizyk.rpc.impl.util;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 
 * 
 * 
 *
 * @author yuyi	
 * @param <T>
 * @param <P>
 */
public class DoubleEntity<T, P> {

	private T entity1;
	private P entity2;

	public static <T, P> DoubleEntity<T, P> builder(T t, P p) {
		return new DoubleEntity<>(t, p);
	}

	public DoubleEntity() {
	}

	public boolean isEmpty() {
		return Objs.anyEmpty(getEntity1(), getEntity2());
	}

	public DoubleEntity(T t, P p) {
		setEntity1(t);
		setEntity2(p);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("entity1", getEntity1())
				.append("entity2", getEntity1()).toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DoubleEntity))
			return false;
		DoubleEntity<?, ?> be = (DoubleEntity<?, ?>) obj;
		return (getEntity1() == be.getEntity1() || (getEntity1() != null && getEntity1().equals(be.getEntity1())))
				&& (getEntity2() == be.getEntity2() || (getEntity2() != null && getEntity2().equals(be.getEntity2())));
	}

	public T getEntity1() {
		return entity1;
	}

	public void setEntity1(T entity1) {
		this.entity1 = entity1;
	}

	public P getEntity2() {
		return entity2;
	}

	public void setEntity2(P entity2) {
		this.entity2 = entity2;
	}

}
