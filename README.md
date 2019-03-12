# ADT

ADT is a framework to perform advanced data transformations on complex data structures.

This project is a part of the research contribution named "Transformation of Complex Data Structures in Big Data Environment". The idea behind it is provide Big Data developers with a powerful transformation framework to transform nested data structures such as arrays by using a concise Domain Specific Language (DSL). 

This is the core project, it implements the Data Transformation Functions engine as well as a Scala-based Domain Specific Language.

ADT has been proven in Apache Spark. The integration with further Big Data tools is a future work.

## Running the case study

Our research contribution is based on a real-world case study which in short consists of the transformation of a Dataset with nested structures. The dataset here presented is an anonymzied dataset from a electricity supplier containning data on its custoemr contracts and power consumption. We have uploaded a version with 1000 rows in the `datasets/power_consumption.json` directory. 

The dataset schema is as follows:

```
 |-- customerID: string (nullable = true)
 |-- tariff: string (nullable = true)
 |-- contractedPower: struct (nullable = true)
 |    |-- period1: double (nullable = true)
 |    |-- period2: double (nullable = true)
 |    |-- period3: double (nullable = true)
 |    |-- period4: double (nullable = true)
 |    |-- period5: double (nullable = true)
 |    |-- period6: double (nullable = true)
 |-- consumption: array (nullable = true)
 |    |-- element: struct (containsNull = true)
 |    |    |-- power: struct (nullable = true)
 |    |    |    |-- period1: double (nullable = true)
 |    |    |    |-- period2: double (nullable = true)
 |    |    |    |-- period3: double (nullable = true)
 |    |    |    |-- period4: double (nullable = true)
 |    |    |    |-- period5: double (nullable = true)
 |    |    |    |-- period6: double (nullable = true)
 |    |    |-- startDate: string (nullable = true)
 |    |    |-- endDate: string (nullable = true)
``` 

This is the Scala code which uses the internal DSL that we have developed. 

```scala
d"ID" < "customerID", //T1
d"T" < "tariff" / translate, //T2
d"CP" * ( //T3
  max("contractedPower.period1", "contractedPower.period4") / asInt,
  max("contractedPower.period2", "contractedPower.period5") / asInt,
  max("contractedPower.period3", "contractedPower.period6") / asInt
),
d"C" * ("consumption" &* ( //T4
  (1 to 3).map(i => max(s"power.period$i", s"power.period${i+3}")) : _*
  )),
d"AVG_C" + ( //T5
  (1 to 3).map(i => d(s"p$i") < avg("consumption" & max(s"power.period$i", s"power.period${i+3}"))) : _*
  ),
d"BD" * ("consumption" & reduce("endDate" / asDate("dd/MM/yyyy"), "startDate" / asDate("dd/MM/yyyy"))(daysBetweenDates)) //T6
)
```
The resulting schema is as follows:

```
root
 |-- ID: string (nullable = true)
 |-- T: integer (nullable = true)
 |-- CP: array (nullable = true)
 |    |-- element: integer (containsNull = true)
 |-- C: array (nullable = true)
 |    |-- element: array (containsNull = true)
 |    |    |-- element: double (containsNull = true)
 |-- AVG_C: struct (nullable = true)
 |    |-- p1: double (nullable = true)
 |    |-- p2: double (nullable = true)
 |    |-- p3: double (nullable = true)
 |-- BD: array (nullable = true)
 |    |-- element: integer (containsNull = true)
```

This transformation has been implemented in a Scala object located at `es.us.idea.adt.Main`. It can be executed by following the following steps:

1 . Apache Spark 2.3.1 is required. If you don't have an Apache Spark cluster, you can execute this example by running it in your local machine. You can download Apache Spark from [here](https://spark.apache.org/downloads.html).

2. Clone this repository

`git clone https://github.com/IDEA-Research-Group/ADT.git`
`cd ADT`

3. Build the jar file

`sbt assembly`

4. Execute the spark-submit command by passing the jar file and the path to the dataset. 

`bin/spark-submit --master local[\*] --class es.us.idea.adt.Main  PATH_TO_JAR PATH_TO_DATASET`

This Apache Spark application outputs a preview of the resulting dataset and its schema.


## Benchmark

TODO

## Future work

Regarding the future work for this project, 

- Generalize the Data Transformation Functions implementation.
- Enrich the DSL grammar.
- Generalize the DSL to support other programming languages.
- Develop connectors to integrate ADT with other Big Data tools.

