package cn.yuyizyk.rpc.domain;

import java.util.Random;
import java.util.UUID;

import org.springframework.stereotype.Service;

import cn.yuyizyk.rpc.api.IRApi2Test;
import cn.yuyizyk.rpc.api.IRTest;

@Service
public class TestService implements IRTest ,IRApi2Test{

	@Override
	public String getNextStr() {
		return "yuyizyk-rpc-demo-service-provide-2@" +UUID.randomUUID().toString();
	}

	@Override
	public int getNextNum() {
		return new Random().nextInt();
	}

}
