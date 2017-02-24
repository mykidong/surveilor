package io.shunters.surveilor.component;

import io.shunters.surveilor.api.component.PutToKafkaHandler;
import io.shunters.surveilor.receiver.VideoStream;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ProduceToKafkaHandler implements PutToKafkaHandler<VideoStream> {

    private static Logger log = LoggerFactory.getLogger(ProduceToKafkaHandler.class);

    private Producer<Integer, VideoStream> producer;

    private boolean sendMessageEnabled;

    public void setSendMessageEnabled(boolean sendMessageEnabled) {
        this.sendMessageEnabled = sendMessageEnabled;
    }

    public void setProducer(Producer<Integer, VideoStream> producer) {
        this.producer = producer;
    }


    @Override
    public void put(VideoStream stream) throws Exception {

        String channelId = stream.getChannelId();

        // send video frame to kafka.
        if (this.sendMessageEnabled) {
            producer.send(new ProducerRecord<Integer, VideoStream>(channelId, stream));
        }
    }
}
