package es.us.idea.ele.xes.dsl

import java.io.File
import java.nio.file.Files

import com.fasterxml.jackson.databind.ObjectMapper
import es.us.idea.adt.data.chameleon._
import es.us.idea.adt.data.chameleon.data.{DataType, SimpleType}
import es.us.idea.adt.data.chameleon.data.complex.{ArrayType, StructType}
import es.us.idea.adt.data.chameleon.dsl.ExpressionContainer
import es.us.idea.adt.data.chameleon.internal.Evaluable
import es.us.idea.adt.data.chameleon.internal.dtf.DTFOperator
import es.us.idea.adt.data.chameleon.internal.dtf.aggregation.{Count, First}
import es.us.idea.adt.data.chameleon.internal.dtf.filter.Filter
import es.us.idea.adt.data.chameleon.internal.dtf.filter.predicates.GreaterThan
import es.us.idea.adt.data.chameleon.internal.dtf.flatten.Flatten
import es.us.idea.adt.data.chameleon.internal.dtf.group.GroupBy
import es.us.idea.ele.json.conversor.{JsonDataConversor, JsonTypeConversor}
import org.kitesdk.data.spi.JsonUtil

object implicits {

  type Eval2DTF = Evaluable => DTFOperator

  sealed trait Interpretable {
    def interpret(data: Any, dataType: DataType): (Any, DataType)
  }

  case class id(path: String) extends Interpretable {
    def interpret(data: Any, dataType: DataType): (Any, DataType) = {
      val evaluable = inferGroupBy(path, dataType)
      val evaluatedDT = evaluable.evaluate(dataType)
      val transformedData = evaluable.applyTransformation(data)
      (transformedData, evaluatedDT)
    }
  }

  case class event(
                    activity: String,
                    criteria: Eval2DTF,
                    timestamp: ExpressionContainer,
                    resource: Option[ExpressionContainer] = None,
                    transactionType: Option[ExpressionContainer] = None,
                    costs: Option[ExpressionContainer] = None,
                    customer: Option[ExpressionContainer] = None
                  ) extends Interpretable {
    // It is expected to receive an ArrayType of StructType. The StructType contains two attributes:
    // __key, which represents the trace ID, and result, which is an Array from where event information is extracted.
    def interpret(data: Any, dataType: DataType): (Any, DataType) = {
      val resultDT =
        dataType match {
          case arrayType: ArrayType => arrayType.getElementDataType match {
            case structType: StructType => structType/*.findAttribute("result").getDataType match {
              case arrayType: ArrayType => arrayType.getElementDataType
              case _ => throw new Exception("event must be created after id")
            }*/
            case _ => throw new Exception("event must be created after id")
          }
          case _ => throw new Exception("event must be created after id")
        }

      //println(s" pinting resultDT $resultDT" )


      //val eventIds = inferGroupBy(activity, resultDT)


      //val part1 =
      //  new Iterate(
      //    new Select(),
      //    new CreateStruct(Seq(
      //      new CreateAttribute("id", new Select("__key")),
      //      new CreateAttribute("event", new Iterate(inferGroupBy(".result."+activity, resultDT),
      //        new CreateStruct(Seq(
      //          new CreateAttribute("name", new Select("__key")),
      //          new CreateAttribute("__temp", new First(
      //            //filter match {
      //            //  case Some(x) => x.apply(new Select("result"))
      //            //  case _ => new Select("result")
      //            //}
      //            filter.apply(new Select("result"))
      //          ))
      //          //new CreateAttribute("result2", new SelectNested("start_date", new First(new Select("result"))))
      //        ))
      //      ))
      //    ))
      //  )

      val part1 =
        new Filter(
          new Iterate(
            new Select(),
            new CreateStruct(Seq(
              new CreateAttribute("id", new Select("__key")),
              new CreateAttribute("event", new Iterate(inferGroupBy(".result."+activity, resultDT),
                new CreateStruct(Seq(
                  new CreateAttribute("name", new Select("__key")),
                  new CreateAttribute("__temp", new First(
                    //filter match {
                    //  case Some(x) => x.apply(new Select("result"))
                    //  case _ => new Select("result")
                    //}
                    criteria.apply(new Select("result"))
                  ))
                  //new CreateAttribute("result2", new SelectNested("start_date", new First(new Select("result"))))
                ))
              ))
            ))
          ),
          new GreaterThan(new Count(new Select("event")), 0)
        )

      val part1DT = part1.evaluate(dataType)
      val part1data = part1.applyTransformation(data)

      val attributes =
        Seq(new CreateAttribute("name", new Select("name"))) ++
        Seq(("timestamp", Some(timestamp)), ("resource", resource), ("type", transactionType), ("costs", costs), ("customer", customer))
        .filter(x => x._2.isDefined)
        .map(x => new CreateAttribute(x._1, new SelectNested("__temp", x._2.get.build())))

      val part2 =
        new Iterate(
          new Select(),
          new CreateStruct(Seq(
            new CreateAttribute("trace", new CreateStruct(Seq(
              new CreateAttribute("id", new Select("id")),
              new CreateAttribute("event", new Iterate(
                new Select("event"),
                new CreateStruct(attributes)
              ))
            )))
          ))
        )

      val part2DT = part2.evaluate(part1DT)
      val part2data = part2.applyTransformation(part1data)

      (part2data, part2DT)

      //new Filter(
      //  new Iterate(
      //    new Select(),
      //    new CreateStruct(Seq(
      //      new CreateAttribute("id", new Select("__key")),
      //      new CreateAttribute("event", new Iterate(new Select("result"),
      //        eventIds
      //      ))
      //    ))
      //  ),
      //  new GreaterThan(new Count(new Select("event")), 0)
      //)
    }
  }

  implicit def expressionContainer2Option(ec: ExpressionContainer) = Some(ec)

  //object event {
  //  def apply(
  //             activity: String,
  //             filter: Eval2DTF,
  //             timestamp: ExpressionContainer,
  //             resource: Option[ExpressionContainer] = None,
  //             transactionType: Option[ExpressionContainer] = None,
  //             costs: Option[ExpressionContainer] = None,
  //             customer: Option[ExpressionContainer] = None
  //           ): event = new event(activity, filter, timestamp, resource, transactionType, costs, customer)
  //}


  //case class modify(str: String)

  object define {

    def trace(id: id) = id
    def trace(event: event) = event

  }

  /**
    * Field
    * */

  //trait Field {
  //  def build(): Evaluable
  //}
  //case class BasicField(path: String) extends Field {
  //  override def build(): Evaluable = new Select(path)
  //}
  //implicit def stringToBasicField(str: String) = BasicField(str)

  //implicit class String2Field(str: String) {
  //}

  /**
    * Entry point
    * */

  case class extract(id: id, event: event) {
    def from(jsonPath: String): Any = {
      // Import java converters
      import scala.collection.JavaConverters._
      // Read File
      val file = new File(jsonPath)
      val fileLines = Files.readAllLines(file.toPath).asScala.toList

      // From json documents to list
      val jsonStr = "[" + fileLines.mkString(", ") + "]"

      // Instantiate the mapper object and get the JSON tree
      val mapper = new ObjectMapper()
      val json = mapper.readTree(jsonStr)

      // Infer  JSON Schema
      val schema = JsonUtil.inferSchema(json,"root")

      // Transform JSON schema to Chameleon schema
      val chameleonSchema = JsonTypeConversor.json2chameleon(schema)
      // Transform JSON Tree to chameleon data type
      val data = JsonDataConversor.json2chameleon(json)

      val idRes = id.interpret(data, chameleonSchema)
      val eventRes = event.interpret(idRes._1, idRes._2)

      eventRes._1

    }
  }

  /**
    * Utility functions
    * */

  // Recibe un array que representa el path hacia el atributo por el que aplicar la agrupación y el datatype raiz
  private def getPathDataTypeTuples(arrPath: Array[String], dataType: DataType, seq: Seq[(String, DataType)] = Seq()): Seq[(String, DataType)] = {
    // arrPath contiene los campos que quedan. la primera posición es el campo a procesar
    // datatype es el tipo de datos de la iteracion anterior


    // el siguiente método, siempre recibe un arrayPath en el que la primera posición es el campo que toca procesar, y el
    // datatype a partir del cual se debe extraer el datatype del campo que toca procesar
    def recursive(arrPath: Array[String], dataType: DataType, seq: Seq[(String, DataType)]): Seq[(String, DataType)] = {
      if(arrPath.length > 0) {

        val thisPath = arrPath.head
        //val remainingPath = arrPath.tail

        dataType match {
          case structType: StructType => {
            val thisDT = structType.findAttribute(thisPath).getDataType
            thisDT match {
              case simpleType: SimpleType => seq :+ (thisPath, thisDT)
              case _ => recursive(arrPath.tail, thisDT, seq :+ (thisPath, thisDT))
            }
          }
          case arrayType: ArrayType => {
            val thisDT = arrayType.getElementDataType
            thisDT match {
              case arrayType: ArrayType => recursive(arrPath, thisDT, seq :+ ("", thisDT)) // El tipo anidado es array, y por lo tanto, thisPath se encuentra en un nivel de anidamiento mayor
              case structType: StructType => recursive(arrPath, thisDT, seq) // El tipo de dato anidado es struct. En la siguiente llamada recursiva se determinará el tipo de este campo
              case _ => throw new Exception("Error interpreting the ID path")
            }
          }
          case _ => throw new Exception("Error interpreting the ID path")
        }
      } else seq
    }


    // lo primero que hay que hacer es comprobar si el primer nivel de arrPath es la raiz. Si es así, instanciar la lista seq
    // con ('', DataType).
    // Si no, instanciarla vacía. El siguiente paso es llamar al método recursivo pasándole el tail de arrPath
    if(arrPath.length > 0) {
      if(arrPath.head == "") recursive(arrPath.tail, dataType, Seq(("", dataType)))
      else recursive(arrPath, dataType, Seq())
    } else throw new Exception("The path is empty")
  }


  def inferGroupBy(path: String, dataType: DataType): Evaluable = {
    //println(s"\n\nprinting datatype $dataType")
    val fixedPath = if(path.charAt(0) != '.') "." + path else path

    val pathArr = fixedPath.split('.')

    val pathDTTuples = getPathDataTypeTuples(pathArr, dataType)

    //println("Imprimiendo pathDTTTuples")
    //println(pathDTTuples.mkString("\n"))
    //println("***")

    // AHora habria que analizar la lista

    // Dividir la lista en dos mitades. La primera mitad contiene todas las tuplas en las tuplas anteriores a la última cuyo datatype es arraytype, inclusive.
    // la segunda mitad contiene las tuplas a continuación de la última tupla cuyo datatype es arraytype.
    val index = pathDTTuples.lastIndexWhere(pair => pair._2.isInstanceOf[ArrayType])

    if(index < 0 || index >= pathDTTuples.length) throw new Exception("The path must contain at least one ArrayType. The last attribute must not be ArrayType")

    val half1 = pathDTTuples.slice(0, index + 1)
    val half2 = pathDTTuples.slice(index + 1, pathDTTuples.length)

    //println("Imprimiendo half1")
    //println(half1.mkString("\n"))
    //println("Imprimiendo half2")
    //println(half2.mkString("\n"))

    //Crear el groupBy. El campo por el que agrupar, que es el segundo argumento de groupby, estará compuesto varios selectNested y un select normal, correspondiente al último
    // elemento de la lista.
    // El campo sobre el que operar se genera mediante una combinación  de flatten/groupby

    def selectFieldToGroupBy(seq: Seq[(String, DataType)]): Evaluable = {
      if(seq.length > 1) new SelectNested(seq.head._1, selectFieldToGroupBy(seq.tail))
      else if (seq.length == 1) new Select(seq.head._1)
      else new Select("")
    }

    def prepareArray(seq: Seq[(String, DataType)]): Evaluable = {
      //println("PrintingSequence\n" + seq.mkString("\n") + "\n***fin***")
      if(seq.length > 1) {
        val current = seq.head
        current._2 match {
          case arrayType: ArrayType => new Flatten(new Iterate(new Select(getStrOrNone(current._1)), prepareArray(seq.tail)))
          case structType: StructType => {
            //println(s"PrepareArray - ${current._1} - $structType")
            if(current._1.isEmpty) prepareArray(seq.tail)
            else new SelectNested(current._1, prepareArray(seq.tail))
          }
          case _ => throw new Exception("DataType must be ComplexType")
        }
      }
      else if (seq.length == 1) new Select(getStrOrNone(seq.head._1))
      else throw new Exception("seq length is less than 1")
    }
    new GroupBy(prepareArray(half1), selectFieldToGroupBy(half2))
  }


  private def getStrOrNone(str: String): Option[String] = {
    if(str.isEmpty) None
    else Some(str)
  }

}
