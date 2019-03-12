package es.us.idea.ele.json.conversor

import es.us.idea.adt.data.chameleon.data.{Attribute, DataType}
import es.us.idea.adt.data.chameleon.data.complex.{ArrayType, StructType}
import es.us.idea.adt.data.chameleon.data.simple._
import org.apache.avro.Schema

object JsonTypeConversor {

  import scala.collection.JavaConverters._

  def json2chameleon(schema: Schema): DataType = {
    /*-RECORD, -ENUM, -ARRAY, -MAP, UNION, FIXED, -STRING, BYTES,
      -INT, -LONG, -FLOAT, -DOUBLE, -BOOLEAN, NULL;*/
    schema.getType() match {
      case Schema.Type.ARRAY => new ArrayType(json2chameleon(schema.getElementType))
      case Schema.Type.RECORD => new StructType(schema.getFields.asScala.map(field => new Attribute(field.name, json2chameleon(field.schema))): _*)
      //case Schema.Type.MAP =>
      case Schema.Type.UNION => {
        val types = schema.getTypes.asScala

        //TODO implementar el caso en el que haya más de dos tipos distintos
        val typesFiltered = types.filter(schema => schema.getType != Schema.Type.NULL)
        if(typesFiltered.length == 1) {
          json2chameleon(types.head)
        } else {
          throw new Exception(s"This schema has many alternatives: $schema")
        }
      }
      case Schema.Type.STRING => new StringType()
      case Schema.Type.INT => new IntegerType()
      case Schema.Type.LONG => new LongType()
      case Schema.Type.FLOAT => new FloatType()
      case Schema.Type.DOUBLE => new DoubleType()
      case Schema.Type.BOOLEAN => new BooleanType()
      case other => throw new Exception(s"Data type could not be converted from Spark to Chameleon: $other \n\t $schema")


    }


  }

}
