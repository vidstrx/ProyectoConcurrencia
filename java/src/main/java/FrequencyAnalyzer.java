import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class FrequencyAnalyzer{
    private String nombre_archivo_output;
    private final int LIMITE = 5000; // Minimum Support
    private final int TOP = 20;

    public record Registro(String palabra, long cantidad) implements Comparable<Registro>{
        @Override
        public int compareTo(Registro otro){
            return Long.compare(otro.cantidad, this.cantidad);
        }
    }
    private List<Registro> filtradas = new ArrayList<>();

    public FrequencyAnalyzer(String nombre_archivo, String carpeta_salida){
        Path dirPath = Paths.get(carpeta_salida);
        if (nombre_archivo.contains("small")){
            if(nombre_archivo.contains("small1"))
                nombre_archivo_output = "small1_frequencyAnalysis";
            else if(nombre_archivo.contains("small2"))
                nombre_archivo_output = "small2_frequencyAnalysis";
            else
                nombre_archivo_output = "small_frequencyAnalysis";
        }else if(nombre_archivo.contains("medium")){
            if(nombre_archivo.contains("medium1"))
                nombre_archivo_output = "medium1_frequencyAnalysis";
            else if(nombre_archivo.contains("medium2"))
                nombre_archivo_output = "medium2_frequencyAnalysis";
            else
                nombre_archivo_output = "medium_frequencyAnalysis";
        }else{
            if(nombre_archivo.contains("large1"))
                nombre_archivo_output = "large1_frequencyAnalysis";
            else if(nombre_archivo.contains("large2"))
                nombre_archivo_output = "large2_frequencyAnalysis";
            else
                nombre_archivo_output = "large_frequencyAnalysis";
        }    

        if(nombre_archivo.contains("00000")){
            nombre_archivo_output += "_part-r-00000";
        }else if(nombre_archivo.contains("00001")){
            nombre_archivo_output += "_part-r-00001";
        }

        try{
            Files.createDirectories(dirPath);
        }catch(IOException e){
            e.printStackTrace();
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(nombre_archivo))){
            BufferedWriter bw = new BufferedWriter(new FileWriter(dirPath.resolve(nombre_archivo_output+".txt").toString()));
            String linea = "";
            // revisa cada linea del resultado del wordcount y para hacer el top 20 palabras
            while((linea = br.readLine()) != null){
                String[] campos = linea.split("\t"); // separar los campos por Tab, ya que asi devuelve el resultado el wordcount
                long cantidad = Long.parseLong(campos[campos.length - 1]); // agarrar la cantidad de apariciones de la palabra o palabras
                if(cantidad >= LIMITE){ // verifica si esa cantidad cumple con el minimum support
                    Registro registro = new Registro(campos[0], cantidad);
                    filtradas.add(registro);
                }
            }
            Collections.sort(filtradas); // ordena de mayor a menor las palabras que pasaron el minimum support

            for (int i = 0; i < TOP && i < filtradas.size(); i++){
                bw.write(filtradas.get(i).palabra() + "\t" + filtradas.get(i).cantidad()); // escribir en el archivo de salidae el top 20
                bw.newLine();
            }
            br.close();
            bw.close();
            System.out.println("Se realizo exitosamente el Frequency Analyzer\nGuardado en la carpeta: " + dirPath.resolve(nombre_archivo_output+".txt").toString());
        }catch(IOException e){
            e.printStackTrace();
            return;
        }
    }
}