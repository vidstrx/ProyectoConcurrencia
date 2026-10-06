# WordCounter / Frequency Analysis

<p align="center">
  <a href="#español">Español</a> | 
  <a href="#english">English</a>
</p>

---

## <a name="español">🇪🇸 Español</a>

## Descripción
Breve explicación de lo que hace tu proyecto.

## Instalación
Pasos para instalar el proyecto:
```bash
git clone https://github.com
```

---

## <a name="english">🇬🇧 English</a>

## Description
This project it's from Concurrency and Distributed Systems class. For this project we used hadoop's MapReduce class to do a wordcount of 1 or 2 pair of words and get the most frequent words from a large dataset using a minimum support of 5000. 
 

## Installation
Steps to install the project:
```bash
git clone https://github.com/vidstrx/ProyectoConcurrencia
```
## Usage
To execute this project you need to have maven and java installed, or use vscode java extensions for correct execution.
After cloning the project, you need to navigate to:
```
cd java/
```

> **Note**: the following commands are for running the project using `maven`

Then you can run the project using this command:

```
mvn exec:java
```

However, to use the `DataPreprocessor`, `WordCount` and `FrequencyAnalyzer`, you got to write the specific arguments of what you want to execute.

```
mvn exec:java -Dargs="<class_to_run> <args_of_the_class>"
```

> **Note**: when you run the project, you will see examples of the arguments on how to run the specific class you want.

For example if you want to run `DataPreprocessor` class, you need to write:

```
mvn exec:java -Dargs="dp <csv_file> <output_dir> <small_limit> <medium_limit> <whitelist_file>"
```

### Wordcount in hadoop
To run the `WordCount` class, you need to have installed and configured hadoop, or you can use a cloud environment that provides a cluster with hadoop preinstalled and preconfigured like [GCP Platform][https://cloud.google.com/], [Azure][https://azure.microsoft.com] or [Amazon Web Services][https://aws.amazon.com].
Hadoop's mapreduce job, needs a jar file that contains the map and reduce classes to execute the wordcount job. That means we need to convert all our classes into a `jar` file.
Run this command to convert our project into a jar file:

```
mvn clean package
```

This command will generate 2 jar files in this location `target/`. The correct jar file that contains Main class its the one named `amazon-reviews-hadoop-1.0.jar`.

> **Recommendation**: for a correct use of the wordcount job, its recommended to clean the csv file using the `DataPreprocessor` class, that makes a `txt` file 

Now that we have the jar file, you can proceed and use hadoop with the following commands:

- **Create a directory**
```
hdfs dfs -mkdir <destination_path_in_hdfs>
```

- **Bring your processed dataset into hdfs**

```
hdfs dfs -put <path_of_local_file> <destination_path_in_hdfs>
```

- **Execute the jar file in hadoop**

```
hadoop jar amazon-reviews-hadoop-1.0.jar wc <word_amount (1 or 2)> <file_path_in_hdfs> <output_path_in_hdfs>
```

- **Get the output file from hdfs to your local machine**

```
hdfs dfs -get <output_file_path_in_hdfs> <destination_path>
```

> **Note**: our project will end up making 2 output files, one for words between a-m and another one between n-z, so you can merge those 2 files into 1 file if you want with: `cat <file1_path> <file2_path> > <merge_file_name_destination>`

### Frequency Analysis
Finally you can execute the `FrequencyAnalyzer` class using mvn or the jar file to get a `txt` file with the top 20 most frequent words from the wordcount output file.
- **Using mvn**
```
mvn exec:java -Dargs="fa <wordcount_output_file> <destination_path>"
```

- **Using the jar file**
```
java -jar amazon-reviews-hadoop-1.0.jar fa <wordcount_output_file> <destination_path>
```
