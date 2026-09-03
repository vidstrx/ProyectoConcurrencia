import csv
import re
import html

archivo_entrada = "../dataset/converted/Automotive.csv"

archivo_small = "../dataset/processed/Automotive_small.txt"
archivo_medium = "../dataset/processed/Automotive_medium.txt"
archivo_large = "../dataset/processed/Automotive_large.txt"

LIMITE_SMALL = 1_000_000
LIMITE_MEDIUM = 3_000_000


def limpiar_texto(texto):
    if texto is None:
        return ""

    texto = str(texto)

    # Convertir entidades HTML
    texto = html.unescape(texto)

    # Convertir todo a minúsculas
    texto = texto.lower()

    # Eliminar URLs
    texto = re.sub(r"https?://\S+|www\.\S+", " ", texto)

    # Eliminar caracteres especiales y signos
    texto = re.sub(r"[^a-z0-9\s]", " ", texto)

    # Eliminar espacios repetidos
    texto = re.sub(r"\s+", " ", texto).strip()

    return texto


total_leidos = 0
total_validos = 0
total_eliminados = 0


with open(archivo_entrada, "r", encoding="utf-8") as entrada, \
     open(archivo_small, "w", encoding="utf-8") as small, \
     open(archivo_medium, "w", encoding="utf-8") as medium, \
     open(archivo_large, "w", encoding="utf-8") as large:

    reader = csv.DictReader(entrada)

    for fila in reader:

        total_leidos += 1

        review = fila.get("reviewText", "")

        review_limpio = limpiar_texto(review)

        # Si queda vacío, no sirve
        if not review_limpio:
            total_eliminados += 1
            continue

        total_validos += 1

        # Dataset grande
        large.write(review_limpio + "\n")

        # Primer millón
        if total_validos <= LIMITE_SMALL:
            small.write(review_limpio + "\n")

        # Primeros tres millones
        if total_validos <= LIMITE_MEDIUM:
            medium.write(review_limpio + "\n")

        if total_leidos % 100000 == 0:
            print(
                f"Leídos: {total_leidos:,} | "
                f"Válidos: {total_validos:,} | "
                f"Eliminados: {total_eliminados:,}"
            )


print("\nPROCESAMIENTO TERMINADO")
print(f"Registros originales leídos: {total_leidos:,}")
print(f"Reseñas válidas: {total_validos:,}")
print(f"Reseñas eliminadas: {total_eliminados:,}")

print("\nArchivos generados:")
print(f"Small:  {archivo_small}")
print(f"Medium: {archivo_medium}")
print(f"Large:  {archivo_large}")
