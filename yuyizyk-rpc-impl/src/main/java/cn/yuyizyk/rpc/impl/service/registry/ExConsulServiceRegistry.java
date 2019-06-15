package cn.yuyizyk.rpc.impl.service.registry;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.cloud.consul.discovery.TtlScheduler;
import org.springframework.stereotype.Component;

import com.ecwid.consul.ConsulException;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;

import lombok.extern.slf4j.Slf4j;

/**
 * 注册服务
 * 
 * 
 *
 * @author yuyi
 */
@Slf4j
@ConditionalOnExpression("#{ '${rpc.close-server:false}' == 'false' }")
@Component
public class ExConsulServiceRegistry implements DisposableBean, ApplicationRunner {
	@Autowired
	private ConsulClient client;
	@Autowired
	private TtlScheduler ttlScheduler;
	@Autowired
	private SpringServiceFinder finder;

	@Autowired
	private RpcServer rpcServer;

	private final List<NewService> services = new ArrayList<>();

	@Value("${rpc.instanceId}")
	private String instanceId;
	@Value("${spring.cloud.consul.discovery.ip-address:localhost}")
	private String ipAddress;
	@Value("${spring.cloud.consul.discovery.heartbeat.ttl-value:30}")
	private Integer ttl = 30;

	public void addService(String serviceName) {
		NewService newService = new NewService();
		newService.setId(instanceId + "-" + serviceName);
		newService.setName(serviceName);
		newService.setPort(rpcServer.getServerPort());
		newService.setAddress(ipAddress);
		NewService.Check check = new NewService.Check();
		check.setTtl(ttl + "s");
		newService.setCheck(check);
		services.add(newService);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		finder.getHandlerMap().keySet().forEach(key -> {
			addService(key);
		});
		services.forEach(newService -> {
			try {
				client.agentServiceRegister(newService);
				log.debug("registry rpc consul.{}.", newService);
				ttlScheduler.add(newService.getId());
			} catch (ConsulException e) {
				log.warn("Failfast is false. Error registering service with consul: " + newService, e);
			}
		});
		log.info("registry rpc consul size[{}]...  OK.", services.size());
	}

	@Override
	public void destroy() throws Exception {
		services.forEach(newService -> {
			ttlScheduler.remove(newService.getId());
			client.agentServiceDeregister(newService.getId());
		});
		log.info("destroy rpc consul... ok.");
	}

}
