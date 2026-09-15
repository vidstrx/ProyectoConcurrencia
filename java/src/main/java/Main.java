import org.apache.hadoop.conf.Configuration;

import org.apache.hadoop.fs.Path;

import org.apache.hadoop.io.IntWritable;

import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;

import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Main {

    public static void main(String[] args) throws Exception{

        if (args.length <= 1 || !(args[0].equals("dp") || args[0].equals("wc") || args[0].equals("fa"))) {
            System.out.println(
                    "Uso: java Main <tipo_trabajo> <args>"
            );

            System.out.println(
                    "\nDataPreprocessor:\njava Main dp <archivo_csv> <carpeta_salida> "
                    + "<limite_small> <limite_medium> <archivo_whitelist>"
            );

            System.out.println(
                    "Ejemplo:"
            );

            System.out.println(
                    "java Main dp Automotive.csv processed "
                    + "1000000 3000000 config/whitelist.txt"
            );

            System.out.println(
                    "\nWordCount:\njava Main wc <cantidad_palabras (1 o 2)> <archivo_entrada> <carpeta_salida> "
            );

            System.out.println(
                    "Ejemplo:\njava Main wc 1 Automotive_medium.txt output"
            );
            
            System.out.println(
                    "\nFrequencyAnalyzer:\njava Main fa <archivo_entrada> <carpeta_salida> "
            );

            System.out.println(
                    "Ejemplo:\njava Main fa medium1_wordcount_output_part-r-00000.txt output"
            );
            return;
        }


        if (args[0].equals("dp")) { // DataPreprocessor

            if (args.length != 6) {

                System.out.println(
                        "Uso:"
                );

                System.out.println(
                        "java Main dp <archivo_csv> <carpeta_salida> "
                        + "<limite_small> <limite_medium> <archivo_stopwords>"
                );

                System.out.println();

                System.out.println(
                        "Ejemplo:"
                );

                System.out.println(
                        "java Main dp Automotive.csv processed "
                        + "1000000 3000000 config/whitelist.txt"
                );

                return;
            }


            String archivoEntrada = args[1];

            String carpetaSalida = args[2];

            long limiteSmall;

            long limiteMedium;


            try {

                limiteSmall =
                        Long.parseLong(args[3]);

                limiteMedium =
                        Long.parseLong(args[4]);

            } catch (NumberFormatException e) {

                System.out.println(
                        "Los límites deben ser números enteros."
                );

                return;
            }


            String archivoWhitelist = args[5];


            if (limiteSmall <= 0 ||
                limiteMedium <= limiteSmall) {

                System.out.println(
                        "Los tamaños no son válidos."
                );

                System.out.println(
                        "El tamaño mediano debe ser mayor "
                        + "que el pequeño."
                );

                return;
            }


            System.out.println(
                    "Iniciando DataPreprocessor..."
            );

            System.out.println(
                    "Dataset: " + archivoEntrada
            );

            System.out.println(
                    "Whitelist: " + archivoWhitelist
            );

            System.out.printf(
                    "Small: %,d reseñas%n",
                    limiteSmall
            );

            System.out.printf(
                    "Medium: %,d reseñas%n",
                    limiteMedium
            );

            System.out.println(
                    "Large: todas las reseñas válidas"
            );

            System.out.println();


            DataPreprocessor.procesarDataset(
                    archivoEntrada,
                    carpetaSalida,
                    limiteSmall,
                    limiteMedium,
                    archivoWhitelist
            );


        } else if (args[0].equals("wc")){ // WordCount

            if (!(args[1].equals("1") || args[1].equals("2")) || args.length != 4) {

                System.out.println(
                        "Uso:\njava Main wc <cantidad_palabras (1 o 2)> <archivo_entrada> <carpeta_salida>\n"
                );

                System.out.println(
                        "Ejemplo:\njava Main wc 1 Automotive_medium.txt output\n"
                );

                return;
            }


            Configuration conf = new Configuration();

            conf.set("tipo.conteo", args[1]); // Cambiar a "2" para pares de palabras

            Job wordCountJob = Job.getInstance(conf, "Word Count " + args[1]);

            wordCountJob.setJarByClass(Main.class);

            wordCountJob.setMapperClass(WordCount.TokenMapper.class);

            wordCountJob.setCombinerClass(WordCount.TokenReducer.class);

            wordCountJob.setPartitionerClass(WordCount.CustomPartitioner.class);

            wordCountJob.setNumReduceTasks(2);

            wordCountJob.setReducerClass(WordCount.TokenReducer.class);

            wordCountJob.setOutputKeyClass(Text.class);

            wordCountJob.setOutputValueClass(IntWritable.class);

            FileInputFormat.addInputPath(wordCountJob, new Path(args[2]));

            FileOutputFormat.setOutputPath(wordCountJob, new Path(args[3]));

            boolean wcSuccess = wordCountJob.waitForCompletion(true);

            System.exit(wcSuccess ? 0 : 1);

            return;
        }else if (args[0].equals("fa")){ //Frequency analysis
                if (args.length != 3) {
                        System.out.println(
                                "Uso:\njava Main fa <archivo_entrada> <carpeta_salida>\n"
                        );
                        System.out.println(
                                "Ejemplo:\njava Main fa medium1_wordcount_output_part-r-00000.txt output\n"
                        );
                        return;
                }
                FrequencyAnalyzer fa = new FrequencyAnalyzer(args[1], args[2]);

        }

        return;
    }
}