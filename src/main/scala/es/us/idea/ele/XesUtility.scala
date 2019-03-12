package es.us.idea.ele

import java.io.{File, PrintWriter}
import java.nio.file.Files

import com.fasterxml.jackson.databind.ObjectMapper
import es.us.idea.adt.data.chameleon.internal.dtfs._
import es.us.idea.ele.json.conversor.{JsonDataConversor, JsonTypeConversor}
import es.us.idea.ele.xes.dsl.XesDataConversor
import org.kitesdk.data.spi.JsonUtil

object XesUtility {

  def main(args: Array[String]) = {

    import es.us.idea.adt.data.chameleon.dsl.implicits._
    import es.us.idea.ele.xes.dsl.implicits._

    val t1 =
      extract(
        define trace id("accode"),
        define trace event(
          activity = "workstation",
          criteria = orderBy(t"start_date" -> toDate("MM/dd/yyyy HH:mm:ss")),
          timestamp = t"start_date" -> toDate("MM/dd/yyyy HH:mm:ss")
        )
      ) from "datasets/json_tipo_allaccodes_oneline.json"

    val t2 =
      extract(
        define trace id("accode"),
        define trace event(
            activity = "gticode",
            criteria = orderBy(t"start_date" -> toDate("MM/dd/yyyy HH:mm:ss")),
            timestamp = t"start_date" -> toDate("MM/dd/yyyy HH:mm:ss")
          )
      )  from "datasets/json_tipo_allaccodes_oneline.json"

    val t3 =
      extract(
        define trace id("gticode"),
        define trace event(
          activity = "incidences.incidencetype",
          criteria = orderBy(t"start_date" -> toDate("MM/dd/yyyy HH:mm:ss")),
          timestamp = t"start_date" -> toDate("MM/dd/yyyy HH:mm:ss"),
          resource = t"incidencecode"
        )
      ) from "datasets/json_tipo_allaccodes_oneline.json"

    val transformed1 = XesDataConversor.chameleon2xes(t1)
    val transformed2 = XesDataConversor.chameleon2xes(t2)
    val transformed3 = XesDataConversor.chameleon2xes(t3)


    val xesStr1 = XesDataConversor.xLogToString(transformed1)
    new PrintWriter("output/T1.xes") { write(xesStr1); close }

    val xesStr2 = XesDataConversor.xLogToString(transformed2)
    new PrintWriter("output/T2.xes") { write(xesStr2); close }

    val xesStr3 = XesDataConversor.xLogToString(transformed3)
    new PrintWriter("output/T3.xes") { write(xesStr3); close }

    println("Xes files have been successfully created.")
  }

}
