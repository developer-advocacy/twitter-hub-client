package com.joshlong.twitter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * somehow this code needs to do the work of talking to our RabbitMQ queues and sending
 * off a request to tweet a <em>scheduled tweet</em>
 *
 * @author Josh Long
 */
@Slf4j
@RequiredArgsConstructor
public class Twitter {

	private final StreamBridge bridge;

	public Mono<Boolean> scheduleTweet(Client client, Date scheduled, String twitterUsername, String jsonRequest) {
		log.debug("clientId: " + client.id());
		log.debug("clientSecret: " + client.secret());
		log.debug("scheduled: " + scheduled);
		log.debug("twitterUsername: " + twitterUsername);
		log.debug("jsonRequest: " + jsonRequest);
		var scheduledString = DateUtils.writeIsoDateTime(scheduled);
		var twitterRequest = new TwitterRequest(client.id(), client.secret(), scheduledString, twitterUsername,
				jsonRequest);
		log.debug("going to send " + twitterRequest);
		try {
			Assert.isTrue(this.bridge.send("twitter-requests", twitterRequest),
					"the message to twitterRequests has not been sent");
			return Mono.just(true);
		} //
		catch (Exception ex) {
			log.error("there's been an exception sending the request", ex);
			return Mono.error(ex);
		}
	}

	public record Client(String id, String secret) {
	}

	record TwitterRequest(String clientId, String clientSecret, String scheduled, String twitterUsername,
			String jsonRequest) {
	}

	/**
	 * yeck.
	 */
	private static abstract class DateUtils {

		public static String writeIsoDateTime(Date in) {
			var ldt = LocalDateTime.ofInstant(in.toInstant(), ZoneId.systemDefault());
			return DateTimeFormatter.ISO_DATE_TIME.format(ldt);
		}

	}

}