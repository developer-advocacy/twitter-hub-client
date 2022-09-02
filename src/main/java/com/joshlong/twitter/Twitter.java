package com.joshlong.twitter;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.util.FileCopyUtils;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

/**
 * Acts as a light facade around the usual Spring Cloud Stream machinery involved in
 * sending requests to the
 * <a href="https://github.com/developer-advocacy/twitter-gateway">Twitter Gateway</a>.
 * You'll need to furnish the requisite RabbitMQ properties in
 * {@code application.properties} to make this work, of course.
 *
 * @author Josh Long
 */
@Slf4j
@RequiredArgsConstructor
public class Twitter {

	private final StreamBridge bridge;

	private final Client defaultClient;

	public Mono<Boolean> scheduleTweet(Date scheduled, String twitterUsername, String text, Media media) {
		Assert.notNull(this.defaultClient, "the defaultClient is null");
		return this.scheduleTweet(this.defaultClient, scheduled, twitterUsername, text, media);
	}

	public Mono<Boolean> scheduleTweet(Client client, Date scheduled, String twitterUsername, String text,
			Media image) {
		var mediaBase64Encoded = (String) null;
		if (null != image && null != image.resource())
			mediaBase64Encoded = base64Encode(image.resource());

		var scheduledString = DateUtils.writeIsoDateTime(scheduled);
		var twitterRequest = new TwitterRequest(client.id(), client.secret(), scheduledString, twitterUsername, text,
				mediaBase64Encoded);

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

	@SneakyThrows
	private static String base64Encode(Resource media) {
		if (null != media) {
			try (var in = media.getInputStream()) {
				var bytes = FileCopyUtils.copyToByteArray(in);
				return Base64Utils.encodeToString(bytes);
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