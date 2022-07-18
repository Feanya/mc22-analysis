package model

import just.semver.SemVer
import slick.jdbc.PostgresProfile.api._
import slick.lifted.ProvenShape
import util.SemVerUtils

import java.sql.Timestamp

/** For matching */
trait DatabaseObject

/** For matching */
trait DatabaseTable

case class MetaResultDB(key: String,
                        value: Int,
                        additional: String) extends DatabaseObject

case class PairResultDB(resultname: String,
                        jarOneID: Int,
                        jarTwoID: Int,
                        versionjump: Int,
                        value: Double) extends DatabaseObject


case class LibraryResultDB(resultname: String,
                           groupid: String,
                           artifactname: String,
                           value: Double) extends DatabaseObject

/**
 * Defining the table of PairResults
 *
 * @param tag implicitly handled by slick
 */
class PairResults(tag: Tag)
  extends Table[PairResultDB](tag, "pairResults")
    with DatabaseTable {

  def * = (resultname, jarOneID, jarTwoID, versionjump, value).mapTo[PairResultDB]

  def resultname = column[String]("resultname")

  def jarOneID: Rep[Int] = column[Int]("jarOneID")

  def jarTwoID: Rep[Int] = column[Int]("jarTwoID")

  def versionjump: Rep[Int] = column[Int]("versionjump")

  def value: Rep[Double] = column[Double]("value")

  def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
}


/**
 * Defining the table of PairResults
 *
 * @param tag implicitly handled by slick
 */
class PairResults2(tag: Tag)
  extends Table[PairResultDB](tag, "pairresult_backup2")
    with DatabaseTable {

  def * = (resultname, jarOneID, jarTwoID, versionjump, value).mapTo[PairResultDB]

  def resultname = column[String]("resultname")

  def jarOneID: Rep[Int] = column[Int]("jarOneID")

  def jarTwoID: Rep[Int] = column[Int]("jarTwoID")

  def versionjump: Rep[Int] = column[Int]("versionjump")

  def value: Rep[Double] = column[Double]("value")

  def exclude: Rep[Boolean] = column[Boolean]("exclude")

  def vinfo: Rep[Boolean] = column[Boolean]("vinfo")

  def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
}


/**
 * Defining the table of MetaResults
 *
 * @param tag implicitly handled by slick
 */
class MetaResults(tag: Tag)
  extends Table[MetaResultDB](tag, "metaResults")
    with DatabaseTable {

  def * = (key, value, additional).mapTo[MetaResultDB]

  def key = column[String]("key")

  def value: Rep[Int] = column[Int]("value")

  def additional: Rep[String] = column[String]("additional")
}

/**
 * Class defining the table of LibraryResults
 *
 * @param tag implicitly handled by slick
 */
class LibraryResults(tag: Tag)
  extends Table[LibraryResultDB](tag, "libraryResults")
    with DatabaseTable {

  def * = (resultname, groupid, artifactname, value).mapTo[LibraryResultDB]

  def resultname = column[String]("resultname")

  def groupid: Rep[String] = column[String]("groupid")

  def artifactname: Rep[String] = column[String]("artifactname")

  def value: Rep[Double] = column[Double]("value")

  def id = column[Int]("id", O.AutoInc, O.PrimaryKey)
}

/**
 * Defining (the types of) the main table
 *
 * @param tag implicitly handled by slick
 */
class Data(tag: Tag)
// todo model rows as objects
  extends Table[JarInfoDB](tag, "data")
    with DatabaseTable {

  def * : ProvenShape[JarInfoDB] =
    (id, groupid, artifactname, classifier, version, path, versionscheme, size).<>(rowToJarInfo, jarInfoToRow)

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

  def path = column[String]("path")

  def versionscheme = column[Int]("versionscheme")

  def classifier = column[Option[String]]("classifier")

  def size = column[Double]("size")

  private def rowToJarInfo(row: (Int, String, String, Option[String], String, String, Int, Double)): JarInfoDB = {
    val SVU = new SemVerUtils()
    val versionsv: SemVer = SVU.parseSemVerString(row._5)
    val jarname: String = s"${row._2}:${row._3}/${row._5}"

    JarInfoDB(row._1, row._2, row._3, row._4, versionsv, row._6, row._7, row._8, jarname)
  }

  def groupid = column[String]("groupid")

  def artifactname = column[String]("artifactname")

  def version = column[String]("version")

  def jarInfoToRow(jarInfoDB: JarInfoDB): Option[(Int, String, String, Option[String], String, String, Int, Double)] = {
    val versionstring: String = jarInfoDB.jarname.split("/").last
    Some(
      (jarInfoDB.id,
        jarInfoDB.groupid,
        jarInfoDB.artifactname,
        jarInfoDB.classifier,
        versionstring,
        jarInfoDB.path,
        jarInfoDB.versionscheme,
        jarInfoDB.size
      ))
  }

  def timestamp = column[Timestamp]("timestamp")
}


/**
 * Defining the table of materialized view aggregated_ga
 *
 * @param tag implicitly handled by slick
 */
class AggregatedGA(tag: Tag)
  extends Table[(Int, String)](tag, "aggregated_ga")
    with DatabaseTable {
  def * = (count, ga)

  def count = column[Int]("count")

  def ga = column[String]("ga")
}