import json
import csv

input_file = "../dataset/raw/Automotive.json"
output_file = "../dataset/processed/Automotive.csv"

fields = [
    "reviewerID",
    "asin",
    "overall",
    "verified",
    "reviewTime",
    "reviewText",
    "summary"
]

total = 0
errores = 0

with open(input_file, "r", encoding="utf-8") as entrada, \
     open(output_file, "w", encoding="utf-8", newline="") as salida:

    writer = csv.DictWriter(salida, fieldnames=fields)
    writer.writeheader()

    for linea in entrada:
        try:
            review = json.loads(linea)

            fila = {}

            for campo in fields:
                valor = review.get(campo, "")

                if isinstance(valor, str):
                    valor = valor.replace("\n", " ").replace("\r", " ")

                fila[campo] = valor

            writer.writerow(fila)
            total += 1

            if total % 100000 == 0:
                print(f"Procesados: {total:,}")

        except (json.JSONDecodeError, UnicodeDecodeError):
            errores += 1

print("\nConversión terminada")
print(f"Registros convertidos: {total:,}")
print(f"Registros con error: {errores:,}")
print(f"Archivo creado: {output_file}")

