package util

import com.typesafe.config.ConfigFactory
import model._
import org.slf4j.{Logger, LoggerFactory}
import slick.jdbc
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

class PostgresUtils(confFromENV: Boolean) {
  val log: Logger = LoggerFactory.getLogger(this.getClass)
  val exceptionlog: ExceptionLogger = new ExceptionLogger("postgres_utils")
  val conn = readConfigAndGetDatabaseConnection(confFromENV)

  createResultTables()

  /** Get GAV-jar-URLs from all vs of a GA
   *
   * @param groupid      G
   * @param artifactname A
   * @return sequence of URLs
   */
  def getJarInfoSemVerOnly(groupid: String, artifactname: String): Seq[JarInfoDB] = {
    log.debug(s"Getting info about jars of $groupid:$artifactname from database…")

    try {

      val action = TableQuery[Data]
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
        .result

      // todo merge
      runAndAwait(action)
    }
    catch {
      case e: Exception => exceptionlog.log(groupid+artifactname, e.getMessage)
        Seq()
    }
  }

  def runAndAwait[T](action: DBIO[T]): T = {
    Await.result(conn.run(action), 1.minute)
  }

  def runAndWait[T](action: DBIO[T]): T = {
    Await.result(conn.run(action), 10.minute)
  }

  /** Get a specific amount of GAs
   *
   * @param limit  maximal amount of libraries
   * @param offset Offset (starting point) - for parallelization purposes
   * @return sequence of URLs
   */
  def getGAs(offset: Int, limit: Int): Seq[(String, String)] = {
    log.info(s"Getting GAs from database…")

    // use view
    val cursorAggregatedGA = TableQuery[AggregatedGA]

    val actionGetGA = cursorAggregatedGA
      .sortBy(_.count)
      .drop(offset)
      .take(limit)
      .map(_.ga)
      .result

    val actionCountGA = cursorAggregatedGA.length.result

    val ga = runAndAwait(actionGetGA)
    val gaCount = runAndAwait(actionCountGA)

    log.info(s"Got ${ga.length}/$gaCount GAs from database, offset $offset")
    ga.foreach(log.debug)

    ga.map(row => // split into groupid and artifactname
      (row.split(':').head, row.split(':').last))

  }

  /** Get some specific GAs
   *
   * @param gas sequence of GAs in the form of G:A
   * @return sequence, but g and a split
   */
  def getGAs(gas: Seq[String]): Seq[(String, String)] = {
    gas.map(row => // split into groupid and artifactname
      (row.split(':').head, row.split(':').last))
  }

  def insertPairResults(results: Iterable[PairResultDB]): Unit = {
    val runInsertFuture: Future[Option[Int]] = run(TableQuery[PairResults] ++= results)
    runInsertFuture.onComplete {
      case Success(a) => log.debug(s"Pair insertion returned: $a")
      case Failure(exception) => log.error(s"Failure while inserting $results! \n$exception")
    }
  }

  private def run[T](action: DBIO[T]): Future[T] = {
    conn.run(action)
  }

  def insertLibraryResults(results: Iterable[LibraryResultDB]): Unit = {
    val runInsertFuture: Future[Option[Int]] = run(TableQuery[LibraryResults] ++= results)
    runInsertFuture.onComplete {
      case Success(a) => log.debug(s"Library insertion returned: $a")
      case Failure(exception) => log.error(s"Failure while inserting $results! \n$exception")
    }
  }

  def insertMetaResults(results: Iterable[MetaResultDB]): Unit = {
    val runInsertFuture: Future[Option[Int]] = run(TableQuery[MetaResults] ++= results)
    runInsertFuture.onComplete {
      case Success(a) => log.debug(s"Meta insertion returned: $a")
      case Failure(exception) => log.error(s"Failure while inserting $results! \n$exception")
    }
  }

  def getDBStats: Map[String, Int] = {
    Map(("PairResults:", runAndAwait(TableQuery[PairResults].length.result)),
      ("LibraryResults:", runAndAwait(TableQuery[LibraryResults].length.result)))
  }

  /** Reads config from resources/application.conf and connects to database */
  private def readConfigAndGetDatabaseConnection(readFromENV: Boolean): jdbc.PostgresProfile.backend.DatabaseDef = {
    var url: String = ""
    var user: String = ""
    var password: String = ""

    if (!readFromENV) {
      log.debug("Reading config…")
      try {
        val conf = ConfigFactory.load()

        url = conf.getString("database.url")
        user = conf.getString("database.user")
        password = conf.getString("database.password")
      }
      catch {
        case e: Exception =>
          log.error(e.getMessage)
          log.error("Try choosing env-mode or provide application.conf!")

          log.info("Reading environment variables…")
          try {
            url = sys.env.getOrElse("POSTGRES_URL", "\n***Loading of environment variable failed: POSTGRES_URL***")
            user = sys.env.getOrElse("POSTGRES_USERNAME", "\n***Loading of environment variable failed: POSTGRES_USERNAME***")
            password = sys.env.getOrElse("POSTGRES_PASSWORD", "\n***Loading of environment variable failed: POSTGRES_PASSWORD***")
          }
          catch {
            case e: Exception =>
              log.error(e.getMessage)
              log.error("Try setting the correct environment variables or do not choose -e mode")
              sys.exit(1)
          }

      }
    }
    else {
      log.info("Reading environment variables…")
      try {
        url = sys.env.getOrElse("POSTGRES_URL", "\n***Loading of environment variable failed: POSTGRES_URL***")
        user = sys.env.getOrElse("POSTGRES_USERNAME", "\n***Loading of environment variable failed: POSTGRES_USERNAME***")
        password = sys.env.getOrElse("POSTGRES_PASSWORD", "\n***Loading of environment variable failed: POSTGRES_PASSWORD***")
      }
      catch {
        case e: Exception =>
          log.error(e.getMessage)
          log.error("Try setting the correct environment variables or do not choose -e mode")
          sys.exit(1)
      }

    }

    log.debug(s"Trying to connect to $user@$url")
    val executor: AsyncExecutor = AsyncExecutor.default("Executor", maxConnections = 20)

    log.debug("Trying to load postgres driver…")
    try {
      Class.forName("org.postgresql.Driver$")
    }
    catch {
      case e: Exception =>
        log.info("Loading didn't work out: org.postgresql.Driver$")
        try {
          Class.forName("org.postgresql.Driver")
        }
        catch {
          case e: Exception => log.info("Loading didn't work out: org.postgresql.Driver")
        }
    }

    Database.forURL(url = url, user, password, executor = executor)
  }

  /** */
  private def createResultTables(): Unit = {
    log.info("Create pair result table if not exists…")
    val actionPairResultTableCreation = TableQuery[PairResults].schema.createIfNotExists
    runAndAwait(actionPairResultTableCreation)

    log.info("Create library result table if not exists…")
    val actionLibraryResultTableCreation = TableQuery[LibraryResults].schema.createIfNotExists
    runAndAwait(actionLibraryResultTableCreation)

    log.info("Create meta result table if not exists…")
    val actionMetaResultTableCreation = TableQuery[MetaResults].schema.createIfNotExists
    runAndAwait(actionMetaResultTableCreation)
  }

  def cleanup(): Unit = {
    log.info("Removing pair result table")
    runAndAwait(TableQuery[PairResults].schema.dropIfExists)

    log.info("Removing library result table")
    runAndAwait(TableQuery[LibraryResults].schema.dropIfExists)

    // necessary to do this again because otherwise we'll have no result tables
    createResultTables()
  }


  /**
   * Hack to close the database connection
   */
  def closeConnection(): Unit = {
    log.info("\uD83D\uDC4B Goodbye database connection!")
    conn.close()
  }

}


