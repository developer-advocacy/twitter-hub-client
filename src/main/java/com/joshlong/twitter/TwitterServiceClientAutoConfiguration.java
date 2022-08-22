package com.joshlong.twitter;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class TwitterServiceClientAutoConfiguration {

	@Bean
	Twitter twitterClient(StreamBridge streamBridge) {
		return new Twitter(streamBridge);
	}

}
