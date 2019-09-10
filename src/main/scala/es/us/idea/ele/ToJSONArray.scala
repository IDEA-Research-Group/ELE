package es.us.idea.ele

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.Files

object ToJSONArray {

  def main(args: Array[String]) = {
    import scala.collection.JavaConverters._

    val file = new File(s"datasets/aircraft_dataset_anonymized.json")

    val fileW = new File(s"datasets/aircraft_dataset_anonymized_array.json")
    val bw = new BufferedWriter(new FileWriter(fileW))

    //Files.readAllLines(file.toPath).asScala.toList.foreach(l =>
    //  bw.write(s"${l.replaceAll("[\\$]", "").replaceAll("numberInt", "numberLong")}\n")
    //)

    val str = s"[${Files.readAllLines(file.toPath).asScala.slice(0, 50).mkString(", ")}] "

    bw.write(str)

    bw.close()


    println("Finished")
  }

}
