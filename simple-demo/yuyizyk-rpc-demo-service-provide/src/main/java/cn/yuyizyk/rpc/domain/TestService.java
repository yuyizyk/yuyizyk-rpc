package cn.yuyizyk.rpc.domain;

import java.util.Random;
import java.util.UUID;

import org.springframework.stereotype.Service;

import cn.yuyizyk.rpc.api.IRTest;

@Service
public class TestService implements IRTest {

	@Override
	public String getNextStr() {
		return "pro1@" +UUID.randomUUID().toString();
	}

	@Override
	public int getNextNum() {
		return new Random().nextInt();
	}

}
