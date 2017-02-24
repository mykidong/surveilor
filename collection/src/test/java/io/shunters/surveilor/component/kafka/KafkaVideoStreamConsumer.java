package io.shunters.surveilor.component.kafka;

import io.shunters.surveilor.receiver.VideoStream;
import io.shunters.surveilor.util.Log4jConfigurer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

public class KafkaVideoStreamConsumer {
	
	private static Logger log = LoggerFactory.getLogger(KafkaVideoStreamConsumer.class);

	private String basedir;

	@Before
	public void init() throws Exception {
		Log4jConfigurer log4j = new Log4jConfigurer();
		log4j.setConfPath("/log4j-test.xml");
		log4j.afterPropertiesSet();

		PropertiesFactoryBean propBean = new PropertiesFactoryBean();
		propBean.setLocation(new ClassPathResource("project.properties"));
		propBean.afterPropertiesSet();

		Properties prop = propBean.getObject();

		basedir = (String) prop.get("project.basedir");
	}

	@Test
	public void consumeEvent() throws InterruptedException {

		Properties props = new Properties();
		props.put("bootstrap.servers", "localhost:9092");
		props.put("group.id", "kafka-video-stream-consumer-test");
		props.put("enable.auto.commit", "true");
		props.put("auto.commit.interval.ms", "1000");
		props.put("session.timeout.ms", "30000");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.IntegerDeserializer");
		props.put("value.deserializer", "io.shunters.surveilor.component.kafka.KafkaAvroVideoStreamDeserializer");
		props.put("video.stream.avro.schema.path", VideoStream.AVRO_SCHEMA);


		// video urls.
		List<String> locations = new ArrayList<>();
		locations.add(basedir + "/src/test/resources/data/paul-gilbert-technical-difficulties.mp4");
		locations.add(basedir + "/src/test/resources/data/zakk-wylde-mr-crowley-solo.mp4");

		for(String location : locations) {
			// channel id.
			String channelId = "" + location.hashCode();
			if (location.contains("/"))
				channelId = location.substring(location.lastIndexOf('/') + 1) + "_" + channelId;

			new Consumer(props, channelId).start();
		}


		Thread.sleep(Long.MAX_VALUE);
	}

	public static class Consumer extends Thread {
		KafkaConsumer<Integer, VideoStream> consumer;
		private final String topic;
		
		private AtomicLong count = new AtomicLong(0);

		public Consumer(Properties props, String topic) {
			consumer = new KafkaConsumer<>(props);
			this.topic = topic;
		}

		public void run() {
			consumer.subscribe(Arrays.asList(topic));

			while (true) {
				ConsumerRecords<Integer, VideoStream> records = consumer.poll(100);
				for (ConsumerRecord<Integer, VideoStream> record : records) {
					VideoStream stream = record.value();

					log.info("offset = {}, key = {}", record.offset(), record.key());
					log.info("channelId: {}, imageBytes: {}", stream.getChannelId(), stream.getImageLength());
				}
			}
		}
	}
}
