import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class DataPreprocessor {

    /*
     * Carga el diccionario de palabras que no son
     * relevantes para el análisis (stopwords).
     */
    public static Set<String> cargarStopwords(String archivoStopwords)
            throws Exception {

        Set<String> stopwords = new HashSet<>();

        for (String linea : Files.readAllLines(
                Path.of(archivoStopwords),
                StandardCharsets.UTF_8)) {

            String palabra =
                    linea.trim().toLowerCase(Locale.ROOT);

            if (!palabra.isEmpty()) {
                stopwords.add(palabra);
            }
        }

        return stopwords;
    }


    /*
     * Limpia el texto de cada reseña.
     */
    public static String limpiarTexto(
            String texto,
            Set<String> stopwords) {

        // 1. Manejar valores nulos o vacíos
        if (texto == null || texto.trim().isEmpty()) {
            return "";
        }

        // 2. Convertir a minúsculas
        texto = texto.toLowerCase(Locale.ROOT);

        // 3. Eliminar URLs
        texto = texto.replaceAll(
                "https?://\\S+|www\\.\\S+",
                " "
        );

        // 4. Eliminar números
        texto = texto.replaceAll(
                "[0-9]+",
                " "
        );

        // 5. Eliminar caracteres especiales
        // Solo quedan letras y espacios
        texto = texto.replaceAll(
                "[^a-z\\s]",
                " "
        );

        // 6. Normalizar espacios
        texto = texto.replaceAll(
                "\\s+",
                " "
        ).trim();

        // 7. Eliminar tokens de una sola letra
        // y palabras contenidas en el diccionario.
        StringBuilder resultado = new StringBuilder();

        for (String palabra : texto.split("\\s+")) {

            if (palabra.length() > 1
                    && !stopwords.contains(palabra)) {

                if (resultado.length() > 0) {
                    resultado.append(" ");
                }

                resultado.append(palabra);
            }
        }

        return resultado.toString().trim();
    }


    /*
     * Procesa el CSV y genera los tres tamaños.
     */
    public static void procesarDataset(
            String archivoEntrada,
            String carpetaSalida,
            long limiteSmall,
            long limiteMedium,
            String archivoStopwords) {

        long totalLeidos = 0;
        long totalValidos = 0;
        long totalEliminados = 0;

        Path salida = Path.of(carpetaSalida);

        Path archivoSmall =
                salida.resolve("Automotive_small.txt");

        Path archivoMedium =
                salida.resolve("Automotive_medium.txt");

        Path archivoLarge =
                salida.resolve("Automotive_large.txt");

        try {

            Files.createDirectories(salida);

            Set<String> stopwords =
                    cargarStopwords(archivoStopwords);

            System.out.println(
                    "Stopwords cargadas: "
                    + stopwords.size()
            );

            try (
                    Reader reader = new InputStreamReader(
                            new FileInputStream(archivoEntrada),
                            StandardCharsets.UTF_8
                    );

                    CSVParser parser = new CSVParser(
                            reader,
                            CSVFormat.DEFAULT.builder()
                                    .setHeader()
                                    .setSkipHeaderRecord(true)
                                    .build()
                    );

                    BufferedWriter small =
                            Files.newBufferedWriter(
                                    archivoSmall,
                                    StandardCharsets.UTF_8
                            );

                    BufferedWriter medium =
                            Files.newBufferedWriter(
                                    archivoMedium,
                                    StandardCharsets.UTF_8
                            );

                    BufferedWriter large =
                            Files.newBufferedWriter(
                                    archivoLarge,
                                    StandardCharsets.UTF_8
                            )
            ) {

                for (CSVRecord registro : parser) {

                    totalLeidos++;

                    String reviewText;

                    try {
                        reviewText =
                                registro.get("reviewText");

                    } catch (Exception e) {

                        totalEliminados++;
                        continue;
                    }

                    String textoLimpio =
                            limpiarTexto(
                                    reviewText,
                                    stopwords
                            );

                    // Si después de toda la limpieza
                    // ya no queda contenido útil,
                    // se elimina la reseña.
                    if (textoLimpio.isEmpty()) {

                        totalEliminados++;
                        continue;
                    }

                    totalValidos++;


                    // LARGE
                    large.write(textoLimpio);
                    large.newLine();


                    // SMALL
                    if (totalValidos <= limiteSmall) {

                        small.write(textoLimpio);
                        small.newLine();
                    }


                    // MEDIUM
                    if (totalValidos <= limiteMedium) {

                        medium.write(textoLimpio);
                        medium.newLine();
                    }


                    // Mostrar progreso
                    if (totalLeidos % 100000 == 0) {

                        System.out.printf(
                                "Leídos: %,d | "
                                + "Válidos: %,d | "
                                + "Eliminados: %,d%n",
                                totalLeidos,
                                totalValidos,
                                totalEliminados
                        );
                    }
                }
            }


            System.out.println();
            System.out.println(
                    "PREPROCESAMIENTO TERMINADO"
            );

            System.out.printf(
                    "Registros originales leídos: %,d%n",
                    totalLeidos
            );

            System.out.printf(
                    "Reseñas válidas: %,d%n",
                    totalValidos
            );

            System.out.printf(
                    "Reseñas eliminadas: %,d%n",
                    totalEliminados
            );

            System.out.println();
            System.out.println("Archivos generados:");

            System.out.println(
                    "Small:  " + archivoSmall
            );

            System.out.println(
                    "Medium: " + archivoMedium
            );

            System.out.println(
                    "Large:  " + archivoLarge
            );

        } catch (Exception e) {

            System.err.println(
                    "Error durante el preprocesamiento:"
            );

            e.printStackTrace();
        }
    }
}
