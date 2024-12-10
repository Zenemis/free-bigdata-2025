package crtracker;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

public class DataCleaner {
    public static class DataCleanMapper
            extends Mapper<Object, Text, Battle, NullWritable>{

        @Override
        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {
            ObjectMapper objectMapper = new ObjectMapper();
            Battle battle = objectMapper.readValue(value.toString(), Battle.class);
            context.write(battle, NullWritable.get());
            }
    }

    public static class DataCleanReducer
            extends Reducer<Battle, NullWritable, Battle, NullWritable> {

        @Override
        public void reduce(Battle key, Iterable<NullWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            context.write(key, NullWritable.get());
        }

    }
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "DataClean-siducamp-ibechoual");
        job.setJarByClass(DataCleaner.class);

        job.setMapperClass(DataCleanMapper.class);
        job.setMapOutputKeyClass(Battle.class);
        job.setMapOutputValueClass(NullWritable.class);

        job.setNumReduceTasks(20);
        job.setReducerClass(DataCleanReducer.class);
        job.setOutputKeyClass(Battle.class);
        job.setOutputValueClass(NullWritable.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
