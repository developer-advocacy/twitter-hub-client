package com.joshlong.twitter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

@Configuration
class TwitterServiceClientAutoConfiguration {

	@Bean
	Twitter twitterClient(StreamBridge streamBridge) {
		return new Twitter(streamBridge);
	}

}

@Slf4j
class TwitterGatewayClientEnvironmentPostProcessor implements EnvironmentPostProcessor {

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		log.debug("registering a Spring Cloud Stream binding property for the `twitterRequests` binding");
		environment.getPropertySources().addLast(new MapPropertySource("twitter-gateway",
				Map.of("spring.cloud.stream.bindings.twitterRequests-out-0.destination", "twitter-requests")));
	}

}
