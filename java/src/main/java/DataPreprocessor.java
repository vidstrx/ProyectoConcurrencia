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

import java.util.Locale;

public class DataPreprocessor {

    /*
     * Limpia el texto de cada reseña.
     */
    public static String limpiarTexto(String texto) {

        // 1. Manejar valores nulos o vacíos
        if (texto == null || texto.trim().isEmpty()) {
            return "";
        }

        // 2. Convertir todo a minúsculas
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
        // Solo se conservan letras y espacios
        texto = texto.replaceAll(
                "[^a-z\\s]",
                " "
        );

        // 6. Eliminar palabras de una sola letra
        // Ejemplos: a, i, s, x
        texto = texto.replaceAll(
                "\\b[a-z]\\b",
                " "
        );

        // 7. Eliminar espacios repetidos
        texto = texto.replaceAll(
                "\\s+",
                " "
        ).trim();

        return texto;
    }


    /*
     * Procesa el CSV completo y genera
     * los tres datasets:
     *
     * Small
     * Medium
     * Large
     */
    public static void procesarDataset(
            String archivoEntrada,
            String carpetaSalida,
            long limiteSmall,
            long limiteMedium) {

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

            // Crear la carpeta de salida si no existe
            Files.createDirectories(salida);

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

                    /*
                     * Solo utilizamos reviewText porque
                     * es el campo necesario para WordCount
                     * y Frequency Analysis.
                     */
                    String reviewText;

                    try {
                        reviewText = registro.get("reviewText");
                    } catch (Exception e) {
                        totalEliminados++;
                        continue;
                    }

                    String textoLimpio =
                            limpiarTexto(reviewText);

                    /*
                     * Si después de la limpieza
                     * la reseña queda vacía,
                     * se descarta.
                     */
                    if (textoLimpio.isEmpty()) {
                        totalEliminados++;
                        continue;
                    }

                    totalValidos++;


                    /*
                     * LARGE
                     * Guarda todas las reseñas válidas.
                     */
                    large.write(textoLimpio);
                    large.newLine();


                    /*
                     * SMALL
                     */
                    if (totalValidos <= limiteSmall) {

                        small.write(textoLimpio);
                        small.newLine();
                    }


                    /*
                     * MEDIUM
                     */
                    if (totalValidos <= limiteMedium) {

                        medium.write(textoLimpio);
                        medium.newLine();
                    }


                    /*
                     * Mostrar progreso cada
                     * 100,000 registros.
                     */
                    if (totalLeidos % 100000 == 0) {

                        System.out.printf(
                                "Leídos: %,d | " +
                                "Válidos: %,d | " +
                                "Eliminados: %,d%n",
                                totalLeidos,
                                totalValidos,
                                totalEliminados
                        );
                    }
                }
            }


            /*
             * Resumen final
             */
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
            System.out.println(
                    "Archivos generados:"
            );

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
