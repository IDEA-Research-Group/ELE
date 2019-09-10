package es.us.idea.ele

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.file.Files

object Main {

  def main(args: Array[String]) = {
    val number = 487

    import scala.collection.JavaConverters._
    // Read File
    val file = new File(s"/home/alvaro/datasets/batch_instance/muestras/angel$number.json")
    //val fileLines = Files.readAllLines(file.toPath).asScala.toList

    // From json documents to list
    //val jsonStr = ("[" + fileLines.mkString(", ") + "]").replaceAll("[\\$]", "")

    val fileW = new File(s"/home/alvaro/datasets/batch_instance/muestras/angel${number}_fixed.json")
    val bw = new BufferedWriter(new FileWriter(fileW))

    Files.readAllLines(file.toPath).asScala.toList.foreach(l =>
      bw.write(s"${l.replaceAll("[\\$]", "").replaceAll("numberInt", "numberLong")}\n")
    )

    bw.close()

  }

}
