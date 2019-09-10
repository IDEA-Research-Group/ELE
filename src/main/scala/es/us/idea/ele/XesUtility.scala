package es.us.idea.ele

import es.us.idea.adt.data.chameleon.data.simple.DateType
import es.us.idea.adt.data.chameleon.internal.dtf.udf.UDF
import es.us.idea.adt.data.chameleon.internal.dtfs._

object XesUtility {

  def main(args: Array[String]) = {

    import es.us.idea.adt.data.chameleon.dsl.implicits._
    import es.us.idea.ele.xes.dsl.implicits._

    val sumaTimestamp = UDF((i: Int) => {
      new java.sql.Date(new java.util.Date().getTime + i)
    }, new DateType)

    val timestamp = UDF((str: String) => {
      new java.sql.Date(str.toLong)
    }, new DateType)

    val number = 487

    //extract(
    //  define trace id("task_id"),
    //  define trace event(
    //    activity = "machine_id",
    //    criteria = orderBy(t"time.numberLong" -> toInt),
    //    timestamp = sumaTimestamp(t"time.numberLong" -> toInt)
    //  )
    //) from s"/home/alvaro/datasets/batch_instance/muestras/angel${number}_fixed.json" save s"/home/alvaro/datasets/batch_instance/muestras/angel${number}_T3.xes"

    //extract(
    //  define trace id("accode"),
    //  define trace event(
    //    activity = "gticode",
    //    criteria =
    //      orderBy(t"start_date" -> toDate("MM/dd/yyyy HH:mm:ss")),
    //    timestamp = t"start_date" -> toDate("MM/dd/yyyy HH:mm:ss")
    //  )
    //) from "datasets/aircraft_dataset_anonymized.json" save "output/TT.xes"

    extract(
      define trace id("job_id.numberLong"),
      define trace event(
        activity = "task_id",
        timestamp = timestamp(t"time.numberLong"),
        criteria =
          orderBy(timestamp(t"time.numberLong"))
      )
    ) from "datasets/muestra-part-487-4.json" save "output/Tmuestra-part-487-4.xes"


    println("Event log extraction has successfully completed.")

  }

}
