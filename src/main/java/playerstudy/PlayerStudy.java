package playerstudy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import scala.xml.Null;

import java.io.IOException;

public class PlayerStudy {
    private static final long BLOCK_SIZE_MB = 128; // Taille du bloc en mégaoctets
    private static int variant= 0 ;

    // Mapper
    public static class PlayerStudyMapper
            extends Mapper<Object, Text, Text, NullWritable> {

        @Override
        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {
            ObjectMapper objectMapper = new ObjectMapper();
            String line = value.toString();

            GameData game = objectMapper.readValue(line, GameData.class);
            if (game.existsPlayers()) {
                context.write(new Text(game.getWinner().tower), NullWritable.get());
                context.write(new Text(game.getLoser().tower), NullWritable.get());
            }
        }
    }

    // Reducer
    public static class PlayerStudyReducer
            extends Reducer<Text, NullWritable, Text, NullWritable> {

        @Override
        public void reduce(Text key, Iterable<NullWritable> values, Context context)
                throws IOException, InterruptedException {
            context.write(key, NullWritable.get());
        }
    }

    // Main
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "PlayerStudy");
        job.setJarByClass(PlayerStudy.class);

        job.setMapperClass(PlayerStudyMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(NullWritable.class);

        job.setReducerClass(PlayerStudyReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        Path inputPath = new Path(args[0]);
        Path outputPath = new Path(args[1]);

        if (args.length > 2) {
            variant = Integer.parseInt(args[2]);
        }

        FileInputFormat.addInputPath(job, inputPath);
        FileOutputFormat.setOutputPath(job, outputPath);

        // Calcul de la taille totale des fichiers d'entrée
        long totalInputSize = getInputSize(inputPath, conf);

        // Calcul du nombre de reducers
        double nbOfBlocks = ((double) totalInputSize) / ((double) (BLOCK_SIZE_MB * 1024 * 1024));
        int numReducers = Math.max(1, (int) Math.ceil(nbOfBlocks));
        job.setNumReduceTasks(numReducers);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

    // Méthode pour calculer la taille totale des fichiers d'entrée
    private static long getInputSize(Path inputPath, Configuration conf) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        long totalSize = 0;

        if (fs.isDirectory(inputPath)) {
            FileStatus[] statuses = fs.listStatus(inputPath);
            for (FileStatus status : statuses) {
                totalSize += status.getLen();
            }
        } else {
            totalSize = fs.getFileStatus(inputPath).getLen();
        }

        return totalSize;
    }
}