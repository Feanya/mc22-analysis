package util

import com.typesafe.config.ConfigFactory
import just.semver.SemVer
import org.slf4j.{Logger, LoggerFactory}
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._

import java.net.URL
import java.sql.Timestamp
import scala.concurrent.Await
import scala.concurrent.duration.Duration


private class Data(tag: Tag) extends Table[(Int, String, String, String, Int)](tag, "data") {
  def path = column[String]("path")

  def classifier = column[Option[String]]("classifier")

  def timestamp = column[Timestamp]("timestamp")

  def * = (id, groupid, artifactname, version, versionscheme)

  def id = column[Int]("id")

  def groupid = column[String]("groupid")

  def artifactname = column[String]("artifactname")

  def version = column[String]("version")

  def versionscheme = column[Int]("versionscheme")
}


private class AggregatedGA(tag: Tag) extends Table[(Int, String)](tag, "aggregated_ga") {
  def * = (vs, ga)

  def vs = column[Int]("vs")

  def ga = column[String]("ga")
}

class PostgresUtils() {
  val log: Logger = LoggerFactory.getLogger("PostgresUtils")

  /** Get GAV-jar-URLs from all vs of a GA
   *
   * @param groupid      G
   * @param artifactname A
   * @return sequence of URLs
   */
  def getURLsAndVersionsSVOnly(groupid: String, artifactname: String): Seq[(URL, SemVer)] = {
    val db = this.readConfigAndGetDatabaseConnection

    log.debug(s"Getting URLs of $groupid:$artifactname from database…")

    try {
      val a = db.run(TableQuery[Data]
        .filter(row =>
          // Correct GA
          row.artifactname === artifactname && row.groupid === groupid
            // only primary artifacts
            && row.classifier.isEmpty
            // only M.M and M.M.P since we don't care about pre-releases
            && (row.versionscheme === 1 || row.versionscheme === 2)
        )
        .sortBy(_.timestamp)
        .map(gav => (gav.path, gav.version))
        .result)

      val urlAndVersionString: Seq[(String, String)] = Await.result(a, Duration.Inf)

      val SVU = new SemVerUtils()
      val urlAndVersion: Seq[(URL, SemVer)] =
        urlAndVersionString.map(v =>
          (new URL(s"https://repo1.maven.org/maven2/${v._1}"),
            SVU.parseSemVerString(v._2)))

      urlAndVersion
    }
    finally {
      db.close()
    }
  }

  /** Get a specific amount of GAs
   * @param limit maximal amount of libraries
   * @return sequence of URLs
   */
  def getGAs(limit: Int): Seq[(String, String)] = {
    val db = readConfigAndGetDatabaseConnection

    log.info(s"Getting GAs from database…")

    try {
      val gavs = TableQuery[AggregatedGA]

      val ga = Await.result(db.run(gavs
        .sortBy(_.ga)
        .take(limit)
        .map(_.ga)
        .result), Duration.Inf)

      val gaCount = Await.result(db.run(gavs.length.result), Duration.Inf)

      log.info(s"Got ${ga.length} out of ${gaCount} GAs from database")
      ga.foreach(ga => log.debug(ga))

      ga.map(row => // split into groupid and artifactname
        (row.split(':').head, row.split(':').last))
    }
    finally {
      db.close()
    }
  }

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

}

