import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class WordCount {
    public static class TokenMapper extends Mapper<Object, Text, Text, IntWritable> {
        private final static IntWritable one = new IntWritable(1);
        private Text palabra = new Text();
        private String tipoConteo;

        @Override
        protected void setup(Context context) {
            // Leer el parámetro definido en el Main
            tipoConteo = context.getConfiguration().get("tipo.conteo", "1");
        }

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String linea = value.toString();
            String[] tokens = linea.split("\\s+"); // Separar por espacios
            
            if (tipoConteo.equals("1")) {
                for (String token : tokens) {
                    palabra.set(token);
                    context.write(palabra, one);
                }
            } else if (tipoConteo.equals("2")) {
                // Lógica para agrupar palabras de dos en dos (pares)
                for (int i = 0; i < tokens.length - 1; i++) {
                    palabra.set(tokens[i] + " " + tokens[i+1]);
                    context.write(palabra, one);
                }
            }
        }
    }

    // CUSTOM PARTITIONER
    public static class CustomPartitioner extends Partitioner<Text, IntWritable> {
        @Override
        public int getPartition(Text key, IntWritable value, int numReduceTasks) {
            // Enviar palabras que empiezan con a-m a un reducer y n-z a otro
            char primeraLetra = key.toString().toLowerCase().charAt(0);
            if (primeraLetra >= 'a' && primeraLetra <= 'm') {
                return 0 % numReduceTasks;
            } else {
                return 1 % numReduceTasks;
            }
        }
    }

    // REDUCER Y COMBINER
    public static class TokenReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable resultado = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            int suma = 0;
            for (IntWritable val : values) {
                suma += val.get();
            }
            resultado.set(suma);
            context.write(key, resultado);
        }
    }
}
