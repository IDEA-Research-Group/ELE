package es.us.idea.ele.json.conversor

import java.sql.Date

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.{ArrayNode, JsonNodeFactory, JsonNodeType, ObjectNode}
import es.us.idea.adt.data.chameleon.internal.utils

import scala.collection.JavaConversions

object JsonDataConversor {

  import scala.collection.JavaConverters._

  /*
  * ARRAY,BINARY,BOOLEAN,MISSING,NULL,NUMBER,OBJECT,POJO,STRING
}*/

  def json2chameleon(jsonNode: JsonNode): Any = {

    jsonNode.getNodeType match {
      case JsonNodeType.ARRAY => jsonNode.elements().asScala.toList.map(x => json2chameleon(x))
      case JsonNodeType.OBJECT => jsonNode.fieldNames().asScala.map(name => (name, json2chameleon(jsonNode.get(name)))).toMap
      case _ => {
        if(jsonNode.isBigDecimal || jsonNode.isFloat || jsonNode.isDouble) Some(jsonNode.asDouble())
        else if(jsonNode.isBigInteger || jsonNode.isLong) Some(jsonNode.asLong())
        else if(jsonNode.isInt || jsonNode.isShort) Some(jsonNode.asInt())
        else if(jsonNode.isTextual) Some(jsonNode.asText())
        else None
      }
    }

  }

  // TODO mejorar
  def chameleon2json(value: Any, objectNode: ObjectNode = JsonNodeFactory.instance.objectNode()): ObjectNode = {

    def addArray(element: Any, arrayNode: ArrayNode): ArrayNode = {
      utils.unwrap(element) match {
        case map: Map[String, Any] => arrayNode.add(chameleon2json(map))
        case seq: Seq[Any] => {
          val arr = JsonNodeFactory.instance.arrayNode()
          //arrayNode.add(chameleon2json(seq))
          seq.foreach(x => addArray(x, arr))
          arrayNode.add(arr)
        }
        case s: String => arrayNode.add(s)
        case i: Int => arrayNode.add(i)
        case l: Long => arrayNode.add(l)
        case d: Double => arrayNode.add(d)
        case f: Float => arrayNode.add(f)
        case date: Date => arrayNode.add(date.getTime)
        case _ => arrayNode.addNull()
      }
    }

    utils.unwrap(value) match {
      case map: Map[String, Any] => map.foldLeft(objectNode)((obj, pair) => chameleon2json(pair, obj) )
      case seq: Seq[Any] => {
        val arr = objectNode.putArray("root")
        seq.foreach(x => addArray(x, arr))
        objectNode
      }
      case (key: String, pairVal: Any) => utils.unwrap(pairVal) match {
        case map: Map[String, Any] => objectNode.set(key, chameleon2json(map)).asInstanceOf[ObjectNode]
        case seq: Seq[Any] => {
          val arr = objectNode.putArray(key)
          seq.foreach(x => addArray(x, arr))
          objectNode
        }
        case s: String => objectNode.put(key, s)
        case i: Int => objectNode.put(key, i)
        case l: Long => objectNode.put(key, l)
        case d: Double => objectNode.put(key, d)
        case f: Float => objectNode.put(key, f)
        case date: Date => objectNode.put(key, date.getTime)
        case _ => objectNode.putNull(key)
      }
      case other => throw new Exception(s"Couldn't transform this node $other")

    }

  }

}
