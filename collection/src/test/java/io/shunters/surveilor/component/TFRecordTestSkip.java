package io.shunters.surveilor.component;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.SQLContext;
import org.junit.Test;
import org.tensorflow.example.*;
import org.tensorflow.hadoop.io.TFRecordFileInputFormat;
import org.tensorflow.hadoop.io.TFRecordFileOutputFormat;
import org.tensorflow.hadoop.shaded.protobuf.ByteString;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TensorFlow Hadoop Input/Output Format Test.
 *
 */
public class TFRecordTestSkip {

    @Test
    public void run() throws Exception {
        SparkConf sparkConf = new SparkConf();
        sparkConf.setMaster("local[3]");
        sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
        sparkConf.set("spark.sql.parquet.compression.codec", "snappy");
        sparkConf.set("spark.streaming.blockInterval", "200");
        sparkConf.setAppName(this.getClass().getName());

        JavaSparkContext ctx = new JavaSparkContext(sparkConf);
        SQLContext sqlCtx = new SQLContext(ctx);

        String output = "target/tfr-output";

        // first, delete the output path.
        FileSystem fs = FileSystem.get(ctx.hadoopConfiguration());
        fs.delete(new Path(output), true);


        List<Map<String, Object>> exampleMapList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {

            long offset = i;
            String text = "hello, this is tf record: " + i;

            Map<String, Object> map = new HashMap<>();
            map.put("offset", offset);
            map.put("text", new String(text));

            exampleMapList.add(map);
        }


        JavaRDD<Map<String, Object>> mapRdd = ctx.parallelize(exampleMapList);

        JavaPairRDD<BytesWritable, NullWritable> examplesPair = mapRdd.mapToPair(new MapToExampleBytes());

        // write examples to tensorflow record file.
        examplesPair.saveAsNewAPIHadoopFile(output, BytesWritable.class, NullWritable.class, TFRecordFileOutputFormat.class);


        // read examples from tensorflow record file.
        JavaPairRDD<BytesWritable, NullWritable> examplesBytesRdd =
                ctx.newAPIHadoopFile(output, TFRecordFileInputFormat.class, BytesWritable.class, NullWritable.class, ctx.hadoopConfiguration());


        JavaRDD<Map<String, Object>> decodedExamples = examplesBytesRdd.map(new ExamplesToMap());


        // print examples.
        for (Map<String, Object> map : decodedExamples.collect()) {
            System.out.printf("text: [%s], offset: [%d]\n", (String) map.get("text"), (Long) map.get("offset"));
        }
    }

    private static class MapToExampleBytes implements PairFunction<Map<String, Object>, BytesWritable, NullWritable> {
        @Override
        public Tuple2<BytesWritable, NullWritable> call(Map<String, Object> map) throws Exception {

            long offset = (Long) map.get("offset");
            String text = (String) map.get("text");

            Int64List int64List = Int64List.newBuilder().addValue(offset).build();
            Feature offsetFeature = Feature.newBuilder().setInt64List(int64List).build();

            ByteString byteString = ByteString.copyFrom(text.getBytes());
            BytesList bytesList = BytesList.newBuilder().addValue(byteString).build();
            Feature textFeature = Feature.newBuilder().setBytesList(bytesList).build();

            Features features = Features.newBuilder()
                    .putFeature("offset", offsetFeature)
                    .putFeature("text", textFeature)
                    .build();
            Example example = Example.newBuilder().setFeatures(features).build();

            return new Tuple2<>(new BytesWritable(example.toByteArray()), NullWritable.get());
        }
    }

    private static class ExamplesToMap implements Function<Tuple2<BytesWritable, NullWritable>, Map<String, Object>> {
        @Override
        public Map<String, Object> call(Tuple2<BytesWritable, NullWritable> t) throws Exception {

            byte[] bytes = t._1().copyBytes();

            Example example = Example.parseFrom(bytes);

            Map<String, Feature> featureMap = example.getFeatures().getFeatureMap();
            byte[] text = featureMap.get("text").getBytesList().getValue(0).toByteArray();

            long offset = featureMap.get("offset").getInt64List().getValue(0);

            Map<String, Object> map = new HashMap<>();

            map.put("offset", offset);
            map.put("text", new String(text));

            return map;
        }
    }
}
