package util

import com.typesafe.config.ConfigFactory
import org.slf4j.{Logger, LoggerFactory}
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._

import java.net.URL
import java.sql.Timestamp
import scala.concurrent.Await
import scala.concurrent.duration.Duration


class Data(tag: Tag) extends Table[(Int, String, String, String, Int)](tag, "data") {
  def id = column[Int]("id")

  def groupid = column[String]("groupid")

  def artifactname = column[String]("artifactname")

  def version = column[String]("version")

  def versionscheme = column[Int]("versionscheme")

  def timestamp = column[Timestamp]("timestamp")

  def * = (id, groupid, artifactname, version, versionscheme)
}

class PostgresUtils() {
  val log: Logger = LoggerFactory.getLogger("")

  private def readConfigAndGetDatabaseConnection: PostgresProfile.backend.DatabaseDef = {
    Class.forName("org.postgresql.Driver")

    log.debug("Reading config…")
    val conf = ConfigFactory.load()

    val url: String = conf.getString("database.url")
    val user: String = conf.getString("database.user")
    val password: String = conf.getString("database.password")

    log.debug(s"Connecting to $user@$url")
    Database.forURL(url, user, password)
  }

  /** Get GAV-jar-URLs from all versions of a GA
   *
   * @param groupid      G
   * @param artifactname A
   * @return sequence of URLs
   */
  def getURLsAllVersions(groupid: String, artifactname: String): Seq[URL] = {
    val db = readConfigAndGetDatabaseConnection

    log.info(s"Getting URLs of $groupid:$artifactname from database…")

    try {
      val gavs = TableQuery[Data]

      log.info("GAVs:")
      //val f = db.run(gavs.take(5).result)
      val a = db.run(gavs
        .filter(row => row.artifactname === artifactname
          && row.groupid === groupid)
        .sortBy(_.timestamp)
        .map(_.version)
        .result)

      val b = Await.result(a, Duration.Inf)
      println(s"$groupid.$artifactname versions in database sorted by timestamp")
      b.distinct.foreach(println(_))

      b.distinct.map(v =>
        new URL(s"https://repo1.maven.org/maven2/${groupid}/${artifactname}/${v}/${artifactname}-${v}.jar")
      )
    }
    finally {
      db.close()
    }
  }

}

