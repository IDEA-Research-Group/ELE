package es.us.idea.ele

import es.us.idea.adt.data.chameleon.internal.dtfs._

object XesUtility {

  def main(args: Array[String]) = {

    import es.us.idea.adt.data.chameleon.dsl.implicits._
    import es.us.idea.ele.xes.dsl.implicits._

      extract(
        define trace id("accode"),
        define trace event(
          activity = "workstation",
          criteria = orderBy(t"start_date" -> toDate("MM/dd/yyyy HH:mm:ss")),
          timestamp = t"start_date" -> toDate("MM/dd/yyyy HH:mm:ss")
        )
      ) from "datasets/aircraft_dataset_anonymized.json" save "output/T1.xes"

      extract(
        define trace id("accode"),
        define trace event(
            activity = "gticode",
            criteria = orderBy(t"start_date" -> toDate("MM/dd/yyyy HH:mm:ss")),
            timestamp = t"start_date" -> toDate("MM/dd/yyyy HH:mm:ss")
          )
      )  from "datasets/aircraft_dataset_anonymized.json" save "output/T2.xes"

      extract(
        define trace id("gticode"),
        define trace event(
          activity = "incidences.incidencetype",
          criteria = orderBy(t"start_date" -> toDate("MM/dd/yyyy HH:mm:ss")),
          timestamp = t"start_date" -> toDate("MM/dd/yyyy HH:mm:ss"),
          resource = t"incidencecode"
        )
      ) from "datasets/aircraft_dataset_anonymized.json" save "output/T3.xes"

    println("Event log extraction has successfully completed.")

  }

}
