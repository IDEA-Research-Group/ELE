# ELE

ELE (Event Log Extractor) is a framework to extract event long from complex data structures.

This project is a part of the research constribution named "Enabling Process Mining in the Internet of Things Environments: Extracting Event Logs from NoSQL Complex Data Structures". The idea behind it is to provide developers and non-expert users with a powerful tool capable of extract event log in XES format to ease process mining tasks. 

This frameworks relies on [Data Chameleon](https://github.com/IDEA-Research-Group/data-chameleon-core), a framework developed by the IDEA Research Group to perform transformations on complex data structures. ELE provides a Domain-Specific Language (DSL) in order to abstract users from the complexity of the transformations needed to extract the event logs from raw logs.

This project is a part of the research contribution named "Transformation of Complex Data Structures in Big Data Environment". The idea behind it is provide Big Data developers with a powerful transformation framework to transform nested data structures such as arrays by using a concise Domain Specific Language (DSL).

ELE has been tested with JSON files obtained from MongoDB databses. The integration with Big Data tools and the support of further data sources is a future work.

1. [Case study](#case-study)
2. [Running the case study](#runnning-case-study)
3. [Using this framework in your project](#running-external)

<a name="case-study"/>
## Case study

Our research contribution is based on a real-world case study which in short consists of the extraction of event log from a raw log which specifies the assembly process of a set of aircrafts. Further information on the characteristics of the dataset can be found in the paper.

In this project, the dataset can be found in the `datasets/aircraft_dataset_anonymized.json` directory. Its schema is shown bellow:

```
root
 |-- workstation: string (nullable = true)
 |-- accode: string (nullable = true)
 |-- gticode: string (nullable = true)
 |-- testcode: string (nullable = true)
 |-- start_date: string (nullable = true)
 |-- executor: string (nullable = true)
 |-- final_date: string (nullable = true)
 |-- successdate: string (nullable = true)
 |-- incidences: array (nullable = true)
 |    |-- element: struct (containsNull = false)
 |    |    |-- incidencecode: string (nullable = true)
 |    |    |-- incidencetype: string (nullable = true)
 |    |    |-- start_date: string (nullable = true)
 |    |    |-- resolution_date: string (nullable = true)
 |    |    |-- label: string (nullable = true)
``` 

The code used to perform the three transformations explained in the paper are shown bellow.

**Test Case A. Process of the aircraft according to the workstation that executes the test.**
```scala
extract(
  define trace id("accode"),
  define trace event(
    activity = "workstation",
    criteria = orderBy(t"start_date" -> toDate("MM/dd/yyyy HH:mm:ss")),
    timestamp = t"start_date" -> toDate("MM/dd/yyyy HH:mm:ss")
  )
) from "datasets/aircraft_dataset_anonymized.json" save "output/T1.xes"
```
The resulting XES file can be found in the directory `output/T1.xes`. Here is an example of the output:
```XML
<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file has been generated with the OpenXES library. It conforms -->
<!-- to the XML serialization of the XES standard for log storage and -->
<!-- management. -->
<!-- XES standard version: 1.0 -->
<!-- OpenXES library version: 1.0RC7 -->
<!-- OpenXES is available from http://www.openxes.org/ -->
<log xes.version="1.0" xes.features="nested-attributes" openxes.version="1.0RC7">
	<trace>
		<string key="concept:name" value="AZ-12407999.O"/>
		<event>
			<string key="concept:name" value="WS-553920"/>
			<date key="time:timestamp" value="2016-05-30T07:54:51.000+02:00"/>
		</event>
		<event>
			<string key="concept:name" value="WS-335429"/>
			<date key="time:timestamp" value="2016-06-23T21:44:20.000+02:00"/>
		</event>
  [...]
	</trace>
 [...]
</log>
```

**Test Case B. Process of the aircraft according to the GTI execution.**
```scala
 extract(
   define trace id("accode"),
   define trace event(
       activity = "gticode",
       criteria = orderBy(t"start_date" -> toDate("MM/dd/yyyy HH:mm:ss")),
       timestamp = t"start_date" -> toDate("MM/dd/yyyy HH:mm:ss")
     )
 )  from "datasets/aircraft_dataset_anonymized.json" save "output/T2.xes"
```
The resulting XES file can be found in the directory `output/T2.xes`. Here is an example of the output:
```XML
<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file has been generated with the OpenXES library. It conforms -->
<!-- to the XML serialization of the XES standard for log storage and -->
<!-- management. -->
<!-- XES standard version: 1.0 -->
<!-- OpenXES library version: 1.0RC7 -->
<!-- OpenXES is available from http://www.openxes.org/ -->
<log xes.version="1.0" xes.features="nested-attributes" openxes.version="1.0RC7">
	<trace>
		<string key="concept:name" value="AZ-12407999.O"/>
		<event>
			<string key="concept:name" value="G-YTT.7.00871445"/>
			<date key="time:timestamp" value="2016-07-22T20:54:57.000+02:00"/>
		</event>
		<event>
			<string key="concept:name" value="T-XTZ.4.00213276"/>
			<date key="time:timestamp" value="2016-06-23T21:51:16.000+02:00"/>
		</event>
  [...]
	</trace>
 [...]
</log>
```

**Test Case C. Process of the incidences according to the type of incidence.**
```scala
 extract(
   define trace id("gticode"),
   define trace event(
     activity = "incidences.incidencetype",
     criteria = orderBy(t"start_date" -> toDate("MM/dd/yyyy HH:mm:ss")),
     timestamp = t"start_date" -> toDate("MM/dd/yyyy HH:mm:ss"),
     resource = t"incidencecode"
   )
 ) from "datasets/aircraft_dataset_anonymized.json" save "output/T3.xes"
```
The resulting XES file can be found in the directory `output/T3.xes`. Here is an example of the output:
```XML
<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file has been generated with the OpenXES library. It conforms -->
<!-- to the XML serialization of the XES standard for log storage and -->
<!-- management. -->
<!-- XES standard version: 1.0 -->
<!-- OpenXES library version: 1.0RC7 -->
<!-- OpenXES is available from http://www.openxes.org/ -->
<log xes.version="1.0" xes.features="nested-attributes" openxes.version="1.0RC7">
	<trace>
		<string key="concept:name" value="C-UYX.5.00877152"/>
		<event>
			<date key="time:timestamp" value="2016-07-05T22:17:24.000+02:00"/>
			<string key="Type" value="IN.O.00375962 PG"/>
			<string key="org:resource" value="IN.O.00375962 PG"/>
			<string key="concept:name" value="O-360"/>
		</event>
	</trace>
	<trace>
		<string key="concept:name" value="H-TSW.2.00456139"/>
		<event>
			<date key="time:timestamp" value="2016-04-25T19:25:27.000+02:00"/>
			<string key="Type" value="IN.N.00687446 QZ"/>
			<string key="org:resource" value="IN.N.00687446 QZ"/>
			<string key="concept:name" value="Q-736"/>
		</event>
	</trace>
 [...]
</log>
```
<a name="runnning-case-study"/>
## Running the case study

The implementation has been carried out in a Scala object located at `es.us.idea.ele.XesUtility`. It can be executed by following the following steps:

1. Clone this repository

`https://github.com/IDEA-Research-Group/ELE.git`
`cd ADT`

2. Execute the class `es.us.idea.ele.XesUtility`

<a name="using-external"/>
## Using this framework in your project

In order to use this framework in a project, you need to import the following Maven dependency:

```XML
<dependency>
    <groupId>es.us.idea</groupId>
    <artifactId>ELE</artifactId>
    <version>0.2.0-SNAPSHOT</version>
</dependency>
```

Don't forget to point at our repositories:
```XML
    <repositories>
        <repository>
            <id>release-repo</id>
            <name>Artifactory-releases-local</name>
            <url>http://estigia.lsi.us.es:1681/artifactory/libs-release-local</url>
            <releases><enabled>true</enabled></releases>
            <snapshots><enabled>false</enabled></snapshots>
        </repository>
        <repository>
            <id>snapshot-repo</id>
            <name>Artifactory-snapshots</name>
            <url>http://estigia.lsi.us.es:1681/artifactory/libs-snapshot-local</url>
            <releases><enabled>false</enabled></releases>
            <snapshots><enabled>true</enabled></snapshots>
        </repository>
    </repositories>
```
You will need the following imports to work with this tool:

1. The ELE DSL.
```scala
import es.us.idea.adt.data.chameleon.internal.dtfs._
import es.us.idea.adt.data.chameleon.dsl.implicits._
import es.us.idea.ele.xes.dsl.implicits._
```

2. The transformation functions from the data-chameleon framework. It enables to transform fields and to specify the criteria to create the event logs..
```scala
import es.us.idea.adt.data.chameleon.internal.dtfs._
```
3. The DSL from the data-chameleon framework. It is usefull to use the transformation functions together with ELE.
```scala
import es.us.idea.adt.data.chameleon.dsl.implicits._
```
