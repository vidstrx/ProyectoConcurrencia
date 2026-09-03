public class Main {

    public static void main(String[] args) {

        if (args.length != 4) {

            System.out.println(
                    "Uso:"
            );

            System.out.println(
                    "java Main <archivo_csv> <carpeta_salida> "
                    + "<limite_small> <limite_medium>"
            );

            System.out.println();
            System.out.println(
                    "Ejemplo:"
            );

            System.out.println(
                    "java Main Automotive.csv processed "
                    + "1000000 3000000"
            );

            return;
        }

        String archivoEntrada = args[0];
        String carpetaSalida = args[1];

        long limiteSmall;
        long limiteMedium;

        try {

            limiteSmall =
                    Long.parseLong(args[2]);

            limiteMedium =
                    Long.parseLong(args[3]);

        } catch (NumberFormatException e) {

            System.out.println(
                    "Los límites deben ser números enteros."
            );

            return;
        }

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
                limiteMedium
        );
    }
}
