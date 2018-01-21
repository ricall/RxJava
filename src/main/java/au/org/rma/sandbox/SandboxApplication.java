package au.org.rma.sandbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;

@SpringBootApplication
@EnableCaching
@EnableHystrix
@EnableHystrixDashboard
public class SandboxApplication {
	public static void main(String[] args) {
		SpringApplication.run(SandboxApplication.class, args);
	}
}
