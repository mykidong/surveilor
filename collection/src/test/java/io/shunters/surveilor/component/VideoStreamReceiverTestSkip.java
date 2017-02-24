package io.shunters.surveilor.component;

import io.shunters.surveilor.receiver.VideoConfiguration;
import io.shunters.surveilor.receiver.VideoStream;
import io.shunters.surveilor.receiver.VideoStreamReceiver;
import io.shunters.surveilor.util.ImageUtils;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.api.java.function.VoidFunction2;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import scala.Tuple2;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Created by mykidong on 2017-02-24.
 */
public class VideoStreamReceiverTestSkip {

    private String basedir;

    @Before
    public void init() throws Exception {
        java.net.URL url = new VideoStreamReceiverTestSkip().getClass().getResource("/log4j-test.xml");
        System.out.println("log4j url: " + url.toString());
        DOMConfigurator.configure(url);

        PropertiesFactoryBean propBean = new PropertiesFactoryBean();
        propBean.setLocation(new ClassPathResource("project.properties"));
        propBean.afterPropertiesSet();

        Properties prop = propBean.getObject();

        basedir = (String) prop.get("project.basedir");
    }


    @Test
    public void run() throws Exception {
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

        VideoConfiguration configuration = new VideoConfiguration();
        configuration.setImageType("jpg");
        configuration.setSamplingSize(15);
        configuration.setStoreBatchSize(10);
        configuration.setLocations(locations);

        JavaDStream<VideoStream> videoStreams = ssc.receiverStream(new VideoStreamReceiver(configuration));

        JavaPairDStream<String, VideoStream> pairStream = videoStreams.mapPartitionsToPair(new StreamsToPair());

        pairStream.foreachRDD(new PutFrames());

        ssc.start();
        ssc.awaitTermination();
    }

    public static class PutFrames implements VoidFunction2<JavaPairRDD<String, VideoStream>, Time> {
        @Override
        public void call(JavaPairRDD<String, VideoStream> rdd, Time time) throws Exception {
            rdd.foreachPartition(new SendFrameToKafka());
        }

        public static class SendFrameToKafka implements VoidFunction<Iterator<Tuple2<String, VideoStream>>> {
            private static Logger log = LoggerFactory.getLogger(SendFrameToKafka.class);

            @Override
            public void call(Iterator<Tuple2<String, VideoStream>> iter) throws Exception {

                int count = 0;

                while (iter.hasNext()) {
                    Tuple2<String, VideoStream> t = iter.next();
                    String channelId = t._1;
                    VideoStream stream = t._2;

                    // TODO: Kafka 로 Frame 관련 VideoStream 전송.


                    // Test 목적으로 image 를 저장해봄.
                    byte[] imageBytes = stream.getImageBytes();
                    BufferedImage image = ImageUtils.bytesToImage(imageBytes);

                    File outputfile = new File("target/" + channelId + "_" + count + "." + stream.getImageType());
                    ImageIO.write(image, stream.getImageType(), outputfile);

                    count++;
                }
            }
        }

    }

    public static class StreamsToPair implements PairFlatMapFunction<Iterator<VideoStream>, String, VideoStream> {
        @Override
        public Iterable<Tuple2<String, VideoStream>> call(Iterator<VideoStream> videoStreamIterator) throws Exception {

            List<Tuple2<String, VideoStream>> tupleList = new ArrayList<>();

            while (videoStreamIterator.hasNext()) {
                VideoStream stream = videoStreamIterator.next();

                tupleList.add(new Tuple2<>(stream.getChannelId(), stream));
            }


            return tupleList;
        }
    }

    @After
    public void shutdown() {

    }
}
