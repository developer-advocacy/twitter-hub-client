package com.joshlong.twitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.util.FileCopyUtils;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;

/**
 * Acts as a light facade around the usual Spring Cloud Stream machinery involved in
 * sending requests to the
 * <a href="https://github.com/developer-advocacy/twitter-gateway">Twitter Gateway</a>.
 * You'll need to furnish the requisite RabbitMQ properties in
 * {@code application.properties} to make this work, of course.
 *
 * @author Josh Long
 */

public class Twitter {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final MessageChannel outbound;

	private final ObjectMapper objectMapper;

	private final Client defaultClient;

	public Twitter(MessageChannel outbound, ObjectMapper objectMapper, Client defaultClient) {
		this.outbound = outbound;
		this.objectMapper = objectMapper;
		this.defaultClient = defaultClient;
	}

	public Mono<Boolean> scheduleTweet(Date scheduled, String twitterUsername, String text, Media media) {
		Assert.notNull(this.defaultClient, "the defaultClient is null");
		return this.scheduleTweet(this.defaultClient, scheduled, twitterUsername, text, media);
	}

	public Mono<Boolean> scheduleTweet(Client client, Date scheduled, String twitterUsername, String text,
			Media image) {
		try {
			var mediaBase64Encoded = (String) null;
			if (null != image && null != image.resource())
				mediaBase64Encoded = base64Encode(image.resource());
			var scheduledString = DateUtils.writeIsoDateTime(scheduled);
			var twitterRequest = new TwitterRequest(client.id(), client.secret(), scheduledString, twitterUsername,
					text, mediaBase64Encoded);
			var json = this.objectMapper.writeValueAsString(twitterRequest);
			log.debug("going to send: " + json);

			var sent = this.outbound.send(MessageBuilder.withPayload(json).build());
			Assert.isTrue(sent, "the message to twitterRequests has not been sent");
			return Mono.just(true);
		} //
		catch (Exception ex) {
			log.error("there's been an exception sending the request", ex);
			return Mono.error(ex);
		}

	}

	private static String base64Encode(Resource media) {
		if (null != media) {
			try (var in = media.getInputStream()) {
				var bytes = FileCopyUtils.copyToByteArray(in);
				return Base64.getEncoder().encodeToString(bytes);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	public record Media(Resource resource, MediaType type) {
	}

	public enum MediaType {

		IMAGE

	}

	public record Client(String id, String secret) {
	}

	record TwitterRequest(String clientId, String clientSecret, String scheduled, String twitterUsername, String text,
			String media) {
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