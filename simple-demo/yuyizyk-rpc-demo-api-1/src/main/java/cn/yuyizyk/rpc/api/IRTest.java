package cn.yuyizyk.rpc.api;

import cn.yuyizyk.rpc.core.anno.RService;

@RService
public interface IRTest {
	public String getNextStr();

	@RService("IRTest.getNextNum")
	public int getNextNum();
}
