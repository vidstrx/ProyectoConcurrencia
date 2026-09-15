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
import java.util.StringTokenizer;

public class DataPreprocessor {

    /*
     * Carga desde un archivo externo las palabras
     * permitidas para el análisis.
     */
    public static Set<String> cargarWhitelist(String archivoWhitelist)
            throws Exception {

        Set<String> whitelist = new HashSet<>();

        for (String linea : Files.readAllLines(
                Path.of(archivoWhitelist),
                StandardCharsets.UTF_8)) {

            String palabra =
                    linea.trim().toLowerCase(Locale.ROOT);

            if (!palabra.isEmpty()) {
                whitelist.add(palabra);
            }
        }

        return whitelist;
    }


    /*
     * Limpia cada reseña y conserva únicamente
     * palabras presentes en la whitelist.
     */
    public static String limpiarTexto(
            String texto,
            Set<String> whitelist) {

        // Validar nulos y vacíos
        if (texto == null || texto.trim().isEmpty()) {
            return "";
        }

        texto = texto.toLowerCase(Locale.ROOT);

        StringBuilder resultado = new StringBuilder();

        /*
         * Primero separamos por espacios.
         */
        StringTokenizer tokenizer =
                new StringTokenizer(texto);

        while (tokenizer.hasMoreTokens()) {

            String token = tokenizer.nextToken();

            // Ignorar URLs
            if (token.startsWith("http://")
                    || token.startsWith("https://")
                    || token.startsWith("www.")) {

                continue;
            }

            /*
             * Separamos las palabras utilizando cualquier
             * carácter que no sea una letra.
             *
             * Ejemplo:
             * "high-quality!!!" -> high / quality
             *
             * No usamos expresiones regulares.
             */
            StringBuilder palabraActual =
                    new StringBuilder();

            for (int i = 0; i <= token.length(); i++) {

                char caracter;

                if (i < token.length()) {
                    caracter = token.charAt(i);
                } else {
                    // Fuerza el procesamiento de la última palabra
                    caracter = ' ';
                }

                if (caracter >= 'a' && caracter <= 'z') {

                    palabraActual.append(caracter);

                } else {

                    if (palabraActual.length() > 0) {

                        String palabra =
                                palabraActual.toString();

                        /*
                         * Solamente conservar palabras
                         * presentes en la whitelist.
                         */
                        if (palabra.length() > 1
                                && whitelist.contains(palabra)) {

                            if (resultado.length() > 0) {
                                resultado.append(" ");
                            }

                            resultado.append(palabra);
                        }

                        palabraActual.setLength(0);
                    }
                }
            }
        }

        return resultado.toString();
    }


    /*
     * Procesa el CSV completo y genera
     * Small, Medium y Large.
     */
    public static void procesarDataset(
            String archivoEntrada,
            String carpetaSalida,
            long limiteSmall,
            long limiteMedium,
            String archivoWhitelist) {

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

            Set<String> whitelist =
                    cargarWhitelist(archivoWhitelist);

            System.out.println(
                    "Palabras cargadas en whitelist: "
                    + whitelist.size()
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
                                    whitelist
                            );

                    /*
                     * Si ninguna palabra de la reseña
                     * pertenece a la whitelist,
                     * se elimina el registro.
                     */
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