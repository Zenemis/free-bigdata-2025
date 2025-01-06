package winratedifference;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/*
25/01/01 12:51:14 INFO mapreduce.Job: Task Id : attempt_1717601749466_7508_m_000001_1, Status : FAILED
Error: com.fasterxml.jackson.core.io.JsonEOFException: Unexpected end-of-input: expected close marker for Object (start marker at [Source: (String)"{"; line: 1, column: 1])
 at [Source: (String)"{"; line: 1, column: 3]
	at com.fasterxml.jackson.core.base.ParserMinimalBase._reportInvalidEOF(ParserMinimalBase.java:588)
	at com.fasterxml.jackson.core.base.ParserBase._handleEOF(ParserBase.java:485)
	at com.fasterxml.jackson.core.base.ParserBase._eofAsNextChar(ParserBase.java:497)
	at com.fasterxml.jackson.core.json.ReaderBasedJsonParser._skipWSOrEnd(ReaderBasedJsonParser.java:2332)
	at com.fasterxml.jackson.core.json.ReaderBasedJsonParser.nextToken(ReaderBasedJsonParser.java:646)
	at com.fasterxml.jackson.databind.deser.BeanDeserializer.deserialize(BeanDeserializer.java:151)
	at com.fasterxml.jackson.databind.ObjectMapper._readMapAndClose(ObjectMapper.java:4001)
	at com.fasterxml.jackson.databind.ObjectMapper.readValue(ObjectMapper.java:2992)
	at winratedifference.WinrateDifference$WinrateMapper.map(WinrateDifference.java:30)
	at winratedifference.WinrateDifference$WinrateMapper.map(WinrateDifference.java:19)
	at org.apache.hadoop.mapreduce.Mapper.run(Mapper.java:146)
	at org.apache.hadoop.mapred.MapTask.runNewMapper(MapTask.java:799)
	at org.apache.hadoop.mapred.MapTask.run(MapTask.java:347)
	at org.apache.hadoop.mapred.YarnChild$2.run(YarnChild.java:174)
	at java.security.AccessController.doPrivileged(Native Method)
	at javax.security.auth.Subject.doAs(Subject.java:422)
	at org.apache.hadoop.security.UserGroupInformation.doAs(UserGroupInformation.java:1688)
	at org.apache.hadoop.mapred.YarnChild.main(YarnChild.java:168)

 */

public class WinrateDifference {

    public static class WinrateMapper extends Mapper<Object, Text, Text, DoubleWritable> {

        private final ObjectMapper objectMapper = new ObjectMapper();

        {
            objectMapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
        }

        @Override
        protected void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            // Parse the JSON object into PlayerData
            WinrateData playerData = objectMapper.readValue(value.toString(), WinrateData.class);

            // Write the ID and winrate to the context
            context.write(new Text(playerData.id), new DoubleWritable(playerData.winrate));
        }
    }

    public static class WinrateReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {

        @Override
        protected void reduce(Text key, Iterable<DoubleWritable> values, Context context) throws IOException, InterruptedException {
            double[] winrates = new double[2];
            int count = 0;

            // Collect winrates for the same ID
            for (DoubleWritable value : values) {
                if (count < 2) {
                    winrates[count] = value.get();
                    count++;
                } else {
                    break;
                }
            }

            // Calculate the difference if both winrates are present
            if (count == 2) {
                double difference = Math.abs(winrates[0] - winrates[1]);
                context.write(key, new DoubleWritable(difference));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Winrate Difference");
        job.setJarByClass(WinrateDifference.class);

        job.setMapperClass(WinrateMapper.class);
        job.setReducerClass(WinrateReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileInputFormat.addInputPath(job, new Path(args[1]));
        FileOutputFormat.setOutputPath(job, new Path(args[2]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
