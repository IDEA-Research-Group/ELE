package es.us.idea.adt.data.chameleon
import es.us.idea.adt.data.chameleon.data.DataType
import es.us.idea.adt.data.chameleon.data.complex.StructType

class Select(name: String) extends Evaluable{

  override def getValue(in: Any): Any = {
    in match {
      case m: Map[String, Any] => m.get(name)
      case Some(a) => getValue(a)
      case _ => None
    }
  }

  override def getDataType(dataType: DataType): DataType = {
    dataType match {
      case struct: StructType => struct.findAttribute(name).getDataType
      case _ => throw new Exception("Select operator must be applied on a Struct data type")
    }
  }
}
