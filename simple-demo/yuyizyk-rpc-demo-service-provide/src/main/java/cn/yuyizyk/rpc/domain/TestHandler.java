package cn.yuyizyk.rpc.domain;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.yuyizyk.rpc.api.IRApi2Test;
import cn.yuyizyk.rpc.api.IRTest;
import cn.yuyizyk.rpc.impl.discovery.RPCProxy;

@RestController
public class TestHandler {

	@Autowired
	private TestService testService;

	@GetMapping("getNextStr")
	public String getNextStr() {
		return testService.getNextStr();
	}

	@GetMapping("getNextNum")
	public int getNextNum() {
		return testService.getNextNum();
	}

	@GetMapping("getNextStr2")
	public String getNextStr2() {
		IRApi2Test api2 = RPCProxy.create(IRApi2Test.class);
		return "IRApi2Test: " + api2.getNextStr();
	}

	@Resource(name = "iRTest")
	private IRTest iRTest;

	@GetMapping("getNextStr3")
	public String getNextStr3() {
		return iRTest.getNextStr();
	}
}
