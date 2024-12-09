package crtracker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class DataCleaner {
    public static class DataCleanMapper
            extends Mapper<LongWritable, Text, BattleKey, BattleValue>{
        private boolean isFirstLine = true;

        @Override
        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
//            if (isFirstLine) {
//                isFirstLine = false; // Ignorer la première ligne
//                return;
//            }
//
//            String line = value.toString();
//            String[] fields = line.split(",");
//
//            // Vérifie qu'il y a au moins 7 champs (évite les erreurs d'index)
//            // et que le champ de population (index 4) n'est pas vide
//            if (fields.length < 7 || fields[4].isEmpty()) {
//                return;
//            }
//
//            Text newKey = new Text(fields[0]+','+fields[2]+','+fields[3]);
//            Text newValue = new Text(fields[4]+','+fields[5]+','+fields[6]);
//            context.write(new Text(newKey), new Text(newValue));
            throw new UnsupportedOperationException();
        }
    }

    public static class DataCleanReducer
            extends Reducer<Text,Text,Text,Text> {

        @Override
        public void reduce(Text key, Iterable<Text> values,
                           Context context
        ) throws IOException, InterruptedException {
//            int maxPopulation = 0;
//            ArrayList<Double> latitudes = new ArrayList<>();
//            ArrayList<Double> longitudes = new ArrayList<>();
//
//            for (Text value : values) {
//                String[] fields = value.toString().split(",");
//                int population = Integer.parseInt(fields[0]);
//                double latitude = Double.parseDouble(fields[1]);
//                double longitude = Double.parseDouble(fields[2]);
//
//                if (population > maxPopulation) {
//                    maxPopulation = population;
//                }
//
//                latitudes.add(latitude);
//                longitudes.add(longitude);
//            }
//
//            Collections.sort(latitudes);
//            Collections.sort(longitudes);
//            double medianLatitude = latitudes.get(latitudes.size() / 2);
//            double medianLongitude = longitudes.get(longitudes.size() / 2);
//
//            context.write(key, new Text(Long.toString(maxPopulation)+','+ medianLatitude +','+ medianLongitude));
            throw new UnsupportedOperationException();
        }

    }
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "TP3");
        job.setNumReduceTasks(1);
        job.setJarByClass(DataCleaner.class);

        job.setMapperClass(DataCleanMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setReducerClass(DataCleanReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setOutputFormatClass(SequenceFileOutputFormat.class);
        job.setInputFormatClass(TextInputFormat.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
