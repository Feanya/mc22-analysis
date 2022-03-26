package util

import com.typesafe.config.ConfigFactory
import model.JarInfo
import org.slf4j.{Logger, LoggerFactory}
import slick.jdbc.PostgresProfile
import slick.jdbc.PostgresProfile.api._

import java.sql.Timestamp
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class PostgresUtils() {
  val log: Logger = LoggerFactory.getLogger("PostgresUtils")

  /** Get GAV-jar-URLs from all vs of a GA
   *
   * @param groupid      G
   * @param artifactname A
   * @return sequence of URLs
   */
  def getJarInfoSemVerOnly(groupid: String, artifactname: String): Seq[(JarInfo)] = {
    val db = this.readConfigAndGetDatabaseConnection

    log.debug(s"Getting info about jars of $groupid:$artifactname from database…")

    try {
      val rowsFuture = db.run(TableQuery[Data]
        .filter(row =>
          // Correct GA
          row.artifactname === artifactname && row.groupid === groupid
            // only primary artifacts
            && row.classifier.isEmpty
            // only M.M and M.M.P since we don't care about pre-releases
            && (row.versionscheme === 1 || row.versionscheme === 2)
        )
        // good first approximation
        .sortBy(_.timestamp)
        .map(gav => (gav.path, gav.version))
        .result)

      val rows = Await.result(rowsFuture, Duration.Inf)
      rows.map(row => new JarInfo(row))
    }
    finally {
      db.close()
    }
  }

  /** Get a specific amount of GAs
   *
   * @param limit  maximal amount of libraries
   * @param offset Offset (starting point) - for parallelization purposes
   * @return sequence of URLs
   */
  def getGAs(limit: Int): Seq[(String, String)] = {
    val db = readConfigAndGetDatabaseConnection

    log.info(s"Getting GAs from database…")

    try {
      // use view
      val cursorAggregatedGA = TableQuery[AggregatedGA]

      val ga = Await.result(db.run(gavs
        .sortBy(_.ga)
        .take(limit)
        .map(_.ga)
        .result), Duration.Inf)

      val gaCount = Await.result(db.run(gavs.length.result), Duration.Inf)

      log.info(s"Got ${ga.length}/${gaCount} GAs from database, offset ${offset}")
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

  /**
   * Private class defining (the types of) the main table
   *
   * @param tag
   */
  private class Data(tag: Tag)
    extends Table[(Int, String, String, Option[String], String, String, Int)](tag, "data") {

    def * =
      (id, groupid, artifactname, classifier, version, path, versionscheme)

    def path = column[String]("path")

    def classifier = column[Option[String]]("classifier")

    def id = column[Int]("id")

    def groupid = column[String]("groupid")

    def artifactname = column[String]("artifactname")

    def version = column[String]("version")

    def versionscheme = column[Int]("versionscheme")

    def timestamp = column[Timestamp]("timestamp")
  }

  /**
   * Private class defining the table of materialized view aggregated_ga
   *
   * @param tag
   */
  private class AggregatedGA(tag: Tag)
    extends Table[(Int, String)](tag, "aggregated_ga") {
    def * = (vs, ga)

    def vs = column[Int]("vs")

    def ga = column[String]("ga")
  }

}

