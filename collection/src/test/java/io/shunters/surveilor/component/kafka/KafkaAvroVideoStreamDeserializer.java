package io.shunters.surveilor.component.kafka;

import io.shunters.surveilor.receiver.VideoStream;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.util.Utf8;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Created by mykidong on 2016-09-20.
 */
public class KafkaAvroVideoStreamDeserializer implements Deserializer<VideoStream> {

    private static Logger log = LoggerFactory.getLogger(KafkaAvroVideoStreamDeserializer.class);

    private Schema schema;

    @Override
    public void configure(Map<String, ?> map, boolean b) {
        String avroSchemaPath = (String) map.get("video.stream.avro.schema.path");

        Schema.Parser parser = new Schema.Parser();
        try {
            schema = parser.parse(getClass().getResourceAsStream(avroSchemaPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public VideoStream deserialize(String s, byte[] bytes) {

        DatumReader<GenericRecord> reader = new GenericDatumReader<GenericRecord>(schema);
        Decoder decoder = DecoderFactory.get().binaryDecoder(bytes, null);

        try {
            GenericRecord genericRecord = reader.read(null, decoder);

            String channelId = ((Utf8) genericRecord.get("channelId")).toString();
            String location = ((Utf8) genericRecord.get("location")).toString();

            int imageLength = (int) genericRecord.get("imageLength");
            byte[] imageBytes = new byte[imageLength];

            ByteBuffer buffer = (ByteBuffer) genericRecord.get("imageBytes");
            buffer.get(imageBytes);


            int imageWidth = (int) genericRecord.get("imageWidth");
            int imageHeight = (int) genericRecord.get("imageHeight");
            String imageType = ((Utf8) genericRecord.get("imageType")).toString();

            long frameTimestamp = (long) genericRecord.get("frameTimestamp");
            long sequenceNo = (long) genericRecord.get("sequenceNo");

            VideoStream stream = new VideoStream(channelId, location, imageBytes, imageWidth, imageHeight, imageType, frameTimestamp, sequenceNo);

            return stream;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {

    }
}
