package evaluation

import com.opencsv.{CSVWriter, ICSVWriter}

import java.io.FileWriter
import scala.collection.JavaConverters._
import scala.util.{Failure, Try}

object utils {


  def writeCsvFile(fileName: String, header: Array[String], rows: List[Array[String]]): Try[Unit] = {
    println(s"Try to write csv $fileName")
    val csvwrite: CSVWriter = new CSVWriter(new FileWriter(fileName),
      ',',
      ICSVWriter.NO_QUOTE_CHARACTER,
      ICSVWriter.DEFAULT_ESCAPE_CHARACTER,
      ICSVWriter.DEFAULT_LINE_END)

    Try(csvwrite).flatMap((csvWriter: CSVWriter) =>
      Try {
        csvWriter.writeNext(header)
        csvWriter.writeAll(rows.asJava)
        csvWriter.close()
      } match {
        case f@Failure(_) =>
          Try(csvWriter.close()).recoverWith {
            case _ => f
          }
        case success =>
          success
      }
    )
  }


  def twoTuplesToRows(tuple: Vector[(Any, Any)]): List[Array[String]] = {
    tuple
      .map(tup => Array(tup._1.toString, tup._2.toString))
      .toList
  }

  def threeTuplesToRows(tuple: Vector[(Any, Any, Any)]): List[Array[String]] = {
    tuple
      .map(tup => Array(tup._1.toString, tup._2.toString, tup._3.toString))
      .toList
  }

  def fourTuplesToRows(tuple: Vector[(Any, Any, Any, Any)]): List[Array[String]] = {
    tuple
      .map(tup => Array(tup._1.toString, tup._2.toString, tup._3.toString, tup._4.toString))
      .toList
  }

  def fiveTuplesToRows(tuple: Vector[(Any, Any, Any, Any, Any)]): List[Array[String]] = {
    tuple
      .map(tup => Array(tup._1.toString, tup._2.toString, tup._3.toString, tup._4.toString, tup._5.toString))
      .toList
  }

  def sevenTuplesToRows(tuple: Vector[(Any, Any, Any, Any, Any, Any, Any)]): List[Array[String]] = {
    tuple
      .map(tup => Array(tup._1.toString, tup._2.toString, tup._3.toString, tup._4.toString, tup._5.toString, tup._6.toString, tup._7.toString))
      .toList
  }

}

