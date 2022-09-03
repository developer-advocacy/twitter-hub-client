package com.joshlong.twitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.StringUtils;

import java.util.Map;

@Configuration
@EnableConfigurationProperties(TwitterServiceClientProperties.class)
class TwitterGatewayClientAutoConfiguration {

	@Bean
	MessageChannel twitterRequestsChannel() {
		return MessageChannels.direct().get();
	}

	@Bean
	IntegrationFlow outboundAmqpIntegrationFlow(MessageChannel twitterRequestsChannel, AmqpTemplate template) {
		var spec = Amqp//
				.outboundAdapter(template)//
				.routingKey("twitter-requests")//
				.exchangeName("twitter-requests");
		return IntegrationFlows//
				.from(twitterRequestsChannel)//
				.handle(spec).get();
	}

	@Bean
	Twitter twitterClient(MessageChannel twitterRequestsChannel, TwitterServiceClientProperties properties,
			ObjectMapper om) {
		var client = properties.client();
		if (null != client) {
			var id = client.id();
			var secret = client.secret();
			var defaultClient = (Twitter.Client) null;
			if (StringUtils.hasText(id) && StringUtils.hasText(secret))
				defaultClient = new Twitter.Client(id, secret);
			return new Twitter(twitterRequestsChannel, om, defaultClient);
		}
		return new Twitter(twitterRequestsChannel, om, null);

	}

}

@ConfigurationProperties(prefix = "twitter")
record TwitterServiceClientProperties(Client client) {

	record Client(String id, String secret) {
	}
}

@Slf4j
class TwitterGatewayClientEnvironmentPostProcessor implements EnvironmentPostProcessor {

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		log.debug("registering a Spring Cloud Stream binding property for the `twitterRequests` binding");
		environment.getPropertySources().addLast(new MapPropertySource("twitter-gateway-propertysource",
				Map.of("spring.cloud.stream.bindings.twitterRequests-out-0.destination", "twitter-requests")));
	}

}
