package io.shunters.surveilor.component;

import io.shunters.surveilor.component.kafka.KafkaAvroVideoStreamDeserializer;
import io.shunters.surveilor.receiver.VideoStream;
import io.shunters.surveilor.util.VideoStreamUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.api.java.function.VoidFunction2;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import scala.Tuple2;

import java.util.*;

/**
 * Created by mykidong on 2017-02-25.
 */
public class FrameFromKafkaTestSkip {

    private String basedir;

    @Before
    public void init() throws Exception {
        java.net.URL url = new FrameFromKafkaTestSkip().getClass().getResource("/log4j-test.xml");
        System.out.println("log4j url: " + url.toString());
        DOMConfigurator.configure(url);

        PropertiesFactoryBean propBean = new PropertiesFactoryBean();
        propBean.setLocation(new ClassPathResource("project.properties"));
        propBean.afterPropertiesSet();

        Properties prop = propBean.getObject();

        basedir = (String) prop.get("project.basedir");
    }

    @Test
    public void run() throws Exception
    {
        // video urls.
        List<String> locations = new ArrayList<>();
        locations.add(basedir + "/src/test/resources/data/paul-gilbert-technical-difficulties.mp4");
        locations.add(basedir + "/src/test/resources/data/zakk-wylde-mr-crowley-solo.mp4");

        // micro batch cycle duration.
        long duration = 5000;

        SparkConf sparkConf = new SparkConf();
        sparkConf.setMaster("local[3]");
        sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
        sparkConf.set("spark.streaming.blockInterval", "200");
        sparkConf.setAppName(this.getClass().getName());

        JavaSparkContext ctx = new JavaSparkContext(sparkConf);
        JavaStreamingContext ssc = new JavaStreamingContext(ctx, new Duration(duration));


        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", "localhost:9092");
        kafkaParams.put("key.deserializer", IntegerDeserializer.class);
        kafkaParams.put("value.deserializer", KafkaAvroVideoStreamDeserializer.class);
        kafkaParams.put("group.id", "frame-from-kafka-group");
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", true);
        kafkaParams.put("video.stream.avro.schema.path", VideoStream.AVRO_SCHEMA);

        List<String> topics = new ArrayList<>();
        for(String location : locations)
        {
            String channelId = VideoStreamUtils.getChannelId(location);
            topics.add(channelId);
        }

        final JavaInputDStream<ConsumerRecord<Integer, VideoStream>> stream =
                KafkaUtils.createDirectStream(ssc, LocationStrategies.PreferConsistent(), ConsumerStrategies.<Integer, VideoStream>Subscribe(topics, kafkaParams));

        JavaPairDStream<String, VideoStream> pairStream = stream.mapPartitionsToPair(new StreamToPair());

        pairStream.foreachRDD(new MakeRdd());

        ssc.start();
        ssc.awaitTermination();
    }

    public static class MakeRdd implements VoidFunction2<JavaPairRDD<String, VideoStream>, Time>
    {
        @Override
        public void call(JavaPairRDD<String, VideoStream> rdd, Time time) throws Exception {

            // TODO: group by 를 한후 rdd 를 pipe() 에 보냄.

            // 단지 Test 목적으로 iteration 함.
            rdd.groupByKey().foreachPartition(new JustPrint());
        }

        public static class JustPrint implements VoidFunction<Iterator<Tuple2<String, Iterable<VideoStream>>>>
        {
            @Override
            public void call(Iterator<Tuple2<String, Iterable<VideoStream>>> iter) throws Exception {

                while(iter.hasNext())
                {
                    Tuple2<String, Iterable<VideoStream>> t = iter.next();

                    String channelId = t._1;

                    Iterator<VideoStream> innerIter = t._2.iterator();
                    while(innerIter.hasNext())
                    {
                        VideoStream stream = innerIter.next();

                        System.out.println("channelId: [" + channelId + "], sequenceNo: [" + stream.getSequenceNo() + "], imageBytes: [" + stream.getImageBytes().length + "]");
                    }
                }
            }
        }
    }

    public static class StreamToPair implements PairFlatMapFunction<Iterator<ConsumerRecord<Integer,VideoStream>>, String, VideoStream>
    {
        @Override
        public Iterator<Tuple2<String, VideoStream>> call(Iterator<ConsumerRecord<Integer, VideoStream>> consumerRecordIterator) throws Exception {

            List<Tuple2<String, VideoStream>> tupleList = new ArrayList<>();

            while (consumerRecordIterator.hasNext())
            {
                ConsumerRecord<Integer, VideoStream> record = consumerRecordIterator.next();

                VideoStream stream = record.value();

                tupleList.add(new Tuple2<>(stream.getChannelId(), stream));
            }


            return tupleList.iterator();
        }
    }
}
