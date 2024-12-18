package listgamemodes;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class ListGameModes {
    private static final long BLOCK_SIZE_MB = 128; // Taille du bloc en mégaoctets

    public static class ListGMMapper
            extends Mapper<Object, Text, GameMode, NullWritable> {

        @Override
        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {
            ObjectMapper objectMapper = new ObjectMapper();

            GameMode gameMode = objectMapper.readValue(value.toString(), GameMode.class);
            context.write(gameMode, NullWritable.get());
        }
    }

    public static class ListGMReducer
            extends Reducer<GameMode, NullWritable, GameMode, NullWritable> {

        @Override
        public void reduce(GameMode key, Iterable<NullWritable> values, Context context)
                throws IOException, InterruptedException {
            context.write(key, NullWritable.get());
        }

    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "DataClean-siducamp-ibechoual");
        job.setJarByClass(ListGameModes.class);

        job.setMapperClass(ListGMMapper.class);
        job.setMapOutputKeyClass(GameMode.class);
        job.setMapOutputValueClass(NullWritable.class);

        job.setReducerClass(ListGMReducer.class);
        job.setOutputKeyClass(GameMode.class);
        job.setOutputValueClass(NullWritable.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        Path inputPath = new Path(args[0]);
        Path outputPath = new Path(args[1]);

        FileInputFormat.addInputPath(job, inputPath);
        FileOutputFormat.setOutputPath(job, outputPath);

        // Calculer la taille totale des fichiers d'entrée
        long totalInputSize = getInputSize(inputPath, conf);

        // Calculer le nombre de reducers dynamiquement
        double nbOfBlocks = ((double) totalInputSize) / ((double) (BLOCK_SIZE_MB * 1024 * 1024));
        int numReducers = Math.max(1, (int) Math.ceil(nbOfBlocks));
        job.setNumReduceTasks(numReducers);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

    /**
     * Calcule la taille totale des fichiers d'entrée en octets.
     *
     * @param inputPath Le chemin d'entrée
     * @param conf      La configuration Hadoop
     * @return La taille totale des fichiers d'entrée en octets
     * @throws IOException En cas d'erreur d'accès au système de fichiers
     */
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
