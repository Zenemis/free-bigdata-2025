package datacleaner;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;

/**
 * Classe principale pour effectuer le nettoyage des données en utilisant Hadoop MapReduce.
 * Ce programme lit des fichiers JSON contenant des objets BattleData, filtre les doublons
 * et les écrit dans un fichier de sortie.
 */
public class DataCleaner {

    // Taille du bloc en mégaoctets pour le calcul du nombre de reducers
    private static final long BLOCK_SIZE_MB = 128;

    /**
     * Mapper pour nettoyer les données. Il lit chaque ligne du fichier d'entrée,
     * essaie de la convertir en un objet BattleData, et si l'objet est valide, l'écrit.
     */
    public static class DataCleanMapper
            extends Mapper<Object, Text, Battle, NullWritable> {


        @Override
        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {

            ObjectMapper objectMapper = new ObjectMapper();
            Battle battle = objectMapper.readValue(value.toString(), Battle.class);

            // Si l'objet BattleData est valide, on l'écrit dans le contexte
            if (battle.isValid()) {
                context.write(battle, NullWritable.get());
            }
        }
    }

    /**
     * Reducer pour nettoyer les données. Il reçoit des objets BattleData et les écrit directement.
     * En l'état, ce reducer ne modifie pas les données mais sert à éliminer les doublons.
     */
    public static class DataCleanReducer
            extends Reducer<Battle, NullWritable, Battle, NullWritable> {

        @Override
        public void reduce(Battle key, Iterable<NullWritable> values, Context context)
                throws IOException, InterruptedException {
            // On écrit simplement chaque clé BattleData reçue dans le contexte
            context.write(key, NullWritable.get());
        }
    }

    /**
     * Méthode principale qui configure et exécute le job MapReduce.
     * Elle définit les classes Mapper et Reducer, le format d'entrée et de sortie,
     * et calcule dynamiquement le nombre de reducers en fonction de la taille des fichiers d'entrée.
     */
    public static void main(String[] args) throws Exception {
        // Configuration Hadoop
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "DataClean-siducamp-ibechoual");
        job.setJarByClass(DataCleaner.class);

        // Définir les classes Mapper et Reducer
        job.setMapperClass(DataCleanMapper.class);
        job.setMapOutputKeyClass(Battle.class);
        job.setMapOutputValueClass(NullWritable.class);

        job.setReducerClass(DataCleanReducer.class);
        job.setOutputKeyClass(Battle.class);
        job.setOutputValueClass(NullWritable.class);

        // Définir les formats d'entrée et de sortie
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        // Chemins d'entrée et de sortie pour les fichiers
        Path inputPath = new Path(args[0]);
        Path outputPath = new Path(args[1]);

        // Ajouter les chemins d'entrée et de sortie au job
        FileInputFormat.addInputPath(job, inputPath);
        FileOutputFormat.setOutputPath(job, outputPath);

        // Calculer la taille totale des fichiers d'entrée
        long totalInputSize = getInputSize(inputPath, conf);

        // Calculer le nombre de reducers en fonction de la taille totale des fichiers
        double nbOfBlocks = ((double) totalInputSize) / ((double) (BLOCK_SIZE_MB * 1024 * 1024));
        int numReducers = Math.max(1, (int) Math.ceil(nbOfBlocks));
        job.setNumReduceTasks(numReducers);

        // Exécuter le job et attendre qu'il se termine
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

    /**
     * Calcule la taille totale des fichiers d'entrée en octets.
     * Cette méthode vérifie si le chemin d'entrée est un répertoire ou un fichier
     * et additionne les tailles des fichiers dans le répertoire si nécessaire.
     *
     * @param inputPath Le chemin d'entrée
     * @param conf      La configuration Hadoop
     * @return La taille totale des fichiers d'entrée en octets
     * @throws IOException En cas d'erreur d'accès au système de fichiers
     */
    private static long getInputSize(Path inputPath, Configuration conf) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        long totalSize = 0;

        // Vérifier si le chemin est un répertoire
        if (fs.isDirectory(inputPath)) {
            FileStatus[] statuses = fs.listStatus(inputPath);
            // Additionner les tailles des fichiers dans le répertoire
            for (FileStatus status : statuses) {
                totalSize += status.getLen();
            }
        } else {
            // Si ce n'est pas un répertoire, on prend directement la taille du fichier
            totalSize = fs.getFileStatus(inputPath).getLen();
        }

        return totalSize;
    }
}
