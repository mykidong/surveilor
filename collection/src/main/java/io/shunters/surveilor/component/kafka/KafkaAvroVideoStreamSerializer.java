package io.shunters.surveilor.component.kafka;

import io.shunters.surveilor.receiver.VideoStream;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;


/**
 * Created by mykidong on 2016-05-17.
 */
public class KafkaAvroVideoStreamSerializer implements Serializer<VideoStream> {

    private static Logger log = LoggerFactory.getLogger(KafkaAvroVideoStreamSerializer.class);

    private Schema schema;


    @Override
    public void configure(Map<String, ?> map, boolean b) {
        String avroSchemaPath = (String) map.get("video.stream.avro.schema.path");

        Schema.Parser parser = new Schema.Parser();
        try {
            schema = parser.parse(getClass().getResourceAsStream(avroSchemaPath));
        }catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] serialize(String s, VideoStream stream) {
        try {
            GenericRecord datum = new GenericData.Record(schema);

            datum.put("channelId", stream.getChannelId());
            datum.put("location", stream.getLocation());

            byte[] imageBytes = stream.getImageBytes();
            ByteBuffer buffer = ByteBuffer.allocate(imageBytes.length);
            buffer.put(imageBytes);
            buffer.rewind();

            datum.put("imageBytes", buffer);
            datum.put("imageLength", imageBytes.length);
            datum.put("imageWidth", stream.getImageWidth());
            datum.put("imageHeight", stream.getImageHeight());
            datum.put("imageType", stream.getImageType());
            datum.put("frameTimestamp", stream.getFrameTimestamp());
            datum.put("sequenceNo", stream.getSequenceNo());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DatumWriter<GenericRecord> writer = new GenericDatumWriter<GenericRecord>(schema);
            Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);
            writer.write(datum, encoder);
            encoder.flush();

            byte[] avroBytes = out.toByteArray();
            out.close();

            return avroBytes;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {

    }
}
