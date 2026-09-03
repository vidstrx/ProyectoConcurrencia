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

public class DataPreprocessor {

    public static String limpiarTexto(String texto) {

        // Manejar valores nulos
        if (texto == null || texto.trim().isEmpty()) {
            return "";
        }

        // Convertir a minúsculas
        texto = texto.toLowerCase();

        // Eliminar URLs
        texto = texto.replaceAll("https?://\\S+|www\\.\\S+", " ");

        // Eliminar caracteres especiales
        // Se conservan letras, números y espacios
        texto = texto.replaceAll("[^a-z0-9\\s]", " ");

        // Normalizar espacios
        texto = texto.replaceAll("\\s+", " ").trim();

        return texto;
    }


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

            // Crear carpeta de salida si no existe
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

                    String reviewText =
                            registro.get("reviewText");

                    String textoLimpio =
                            limpiarTexto(reviewText);

                    // Eliminar registros sin texto útil
                    if (textoLimpio.isEmpty()) {
                        totalEliminados++;
                        continue;
                    }

                    totalValidos++;

                    // Dataset grande:
                    // todas las reseñas válidas
                    large.write(textoLimpio);
                    large.newLine();

                    // Dataset pequeño
                    if (totalValidos <= limiteSmall) {
                        small.write(textoLimpio);
                        small.newLine();
                    }

                    // Dataset mediano
                    if (totalValidos <= limiteMedium) {
                        medium.write(textoLimpio);
                        medium.newLine();
                    }

                    if (totalLeidos % 100000 == 0) {

                        System.out.printf(
                                "Leídos: %,d | Válidos: %,d | Eliminados: %,d%n",
                                totalLeidos,
                                totalValidos,
                                totalEliminados
                        );
                    }
                }
            }

            System.out.println();
            System.out.println("PREPROCESAMIENTO TERMINADO");
            System.out.printf(
                    "Registros leídos: %,d%n",
                    totalLeidos
            );

            System.out.printf(
                    "Registros válidos: %,d%n",
                    totalValidos
            );

            System.out.printf(
                    "Registros eliminados: %,d%n",
                    totalEliminados
            );

            System.out.println();
            System.out.println("Archivos generados:");
            System.out.println(archivoSmall);
            System.out.println(archivoMedium);
            System.out.println(archivoLarge);

        } catch (Exception e) {

            System.err.println(
                    "Error durante el preprocesamiento:"
            );

            e.printStackTrace();
        }
    }
}
