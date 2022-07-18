package evaluation

import analysis.NamedAnalysis
import application.AnalysisApplication
import com.opencsv._
import model.MetaResults
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery
import util.PostgresUtils

import java.io.FileWriter
import scala.collection.JavaConverters._
import scala.util.{Failure, Try}


object Filter extends AnalysisApplication{
  var cfg: CliConfig = CliConfig()
  val postgresInteractor = new PostgresUtils(cfg.environmentvariables)

  final override def main(args: Array[String]): Unit = {
    cfg = parseArguments(args)

    println("Me filtering unit \uD83D\uDE80")

    println("Create columns for version info")
    postgresInteractor.runAndAwait(
      sqlu"""alter table data add column if not exists vinfo varchar""")
    postgresInteractor.runAndAwait(
      sqlu"""alter table pairresult_backup2 add column if not exists vinfo varchar"""
    )
    postgresInteractor.runAndAwait(
      sqlu"""alter table libdedup add column if not exists vinfo varchar"""
    )
    postgresInteractor.runAndAwait(
      sqlu"""alter table libdedup add column if not exists exclude boolean"""
    )
    postgresInteractor.runAndAwait(
      sqlu"""alter table pairresult_backup2 add column if not exists exclude boolean"""
    )

    println("Fill in flags")
    val q = for {datarow <- TableQuery[MetaResults] if datarow.key like "%"} yield datarow.additional
    postgresInteractor.runAndAwait(q.update(""))


    //val r = for {datarow <- TableQuery[PairResults2] if datarow.id === 1} yield datarow.exclude
    //postgresInteractor.runAndAwait(r.update(false))


    // println("Create view on overall upgrades by year")
    // createViewOnUpgradesWithYear(postgresInteractor)



    //createViewOnUpgradeIds(postgresInteractor)
    //createViewPairsWithTimestamp(postgresInteractor)
    //createViewPrimaries(postgresInteractor)

    //reproduceRaemaekers()
    study()

    postgresInteractor.closeConnection()

  }

  def reproduceRaemaekers(): Unit = {
    log.info("Reproduce Raemaekers")
    val driver = new RaemaekersPaper()
    driver.reproduceRPtable1()
    driver.myrpTable1Alltime()
    //driver.rptable2()
    //for(i <- 0 to 6)
    //  driver.tab_3_upgrades_by_year(i)
  }

  def dataset_data(): Unit = {
    log.info("Generate data for descriptive statistics")
    val driver = new Datasetpaper()
    driver.jars_by_year_in_j_and_a()
      }

  def study(): Unit = {
   log.info("Answer RQ1")
   val driver = new RQ1()
  //  driver.one_a()
    driver.one_az()
    driver.one_anz()
  //  driver.one_b()
//    driver.one_c("CWronglyRemoved", 0)
//    driver.one_c("CWronglyRemoved", 1)
//    for(i <- 3 to 4)
//      driver.one_c("CWronglyRemoved", i)
//    driver.one_c("MWronglyRemovedPub", 0)
//    driver.one_c("MWronglyRemovedPub", 1)
//    for(i <- 3 to 4)
//      driver.one_c("MWronglyRemovedPub", i)
    // driver.one_ctot()

    log.info("Answer RQ2")
    val driverb = new RQ2()
    println("Calculating versionschemes by year")
    //for(i <- 1 to 6)
    //  driverb.two_a(i)
    //println("Calculating all upgrades by year")
    // for(i <- 2 to 6)
    //  driverb.two_b(i)
    //for(i <- 0 to 1)
    //  driverb.two_ctot(i)
    //for(i <- 3 to 6)
    //  driverb.two_ctot(i)

    log.info("Answer RQ4")
    val driverd = new RQ4()
    //driverd.four_a()
    //driverd.four_ac("CDeprecatedInPrev")
    //driverd.four_ac("MDeprecatedInPrevPub")
    //driverd.four_atc()
    //driverd.four_atw()
    //driverd.four_atwp()
    //for(i <- 0 to 1)
    //  driverd.four_b(i)
    //for(i <- 3 to 6)
    //  driverd.four_b(i)
    // driverd.four_b_totdeprm()
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

  private def createViewOnUpgradesWithYear(postgresInteractor: PostgresUtils): Unit = {

    println("Creating view")
    val createUpgradeView = postgresInteractor.runAndWait(
      sqlu"""CREATE OR REPLACE VIEW upgrade_years AS
            (SELECT p.id, EXTRACT(YEAR FROM d.timestamp) AS year
            FROM pairresult_backup2 AS p
            JOIN data d on d.id = p."jarTwoID")
         """)

    assert(createUpgradeView == 0)
    println("Done!")
    println("Test view:")
    val testView = postgresInteractor.runAndWait(
      sql"""SELECT * FROM upgrade_years LIMIT 5""".as[(Int, Int)])

    println(testView)
  }


  private def createViewOnUpgradeIds(postgresInteractor: PostgresUtils): Unit = {
    println("Materializing view on jars that are contained in an upgrade")
    val createView = postgresInteractor.runAndWait(
      sqlu"""
             CREATE MATERIALIZED VIEW IF NOT EXISTS allids_with_timestamp AS
                SELECT ids, timestamp FROM
                ((SELECT "jarOneID" AS ids FROM pairresult_backup2)
                UNION DISTINCT
                (SELECT "jarTwoID" AS ids FROM pairresult_backup2)) upgradeids
                JOIN data ON upgradeids.ids=data.id
                """)
    assert(createView == 0)
    println("Done")
  }

  private def createViewPairsWithTimestamp(postgresInteractor: PostgresUtils): Unit = {
    println("Materializing view on jars that are contained in an upgrade")
    val createView = postgresInteractor.runAndWait(
      sqlu"""
             CREATE OR REPLACE VIEW pairresult_backup_with_timestamp AS
                SELECT alltime.*, timestamps.timestamp
                FROM
                    (SELECT * FROM pairresult_backup2) AS alltime
                        JOIN
                        (SELECT * FROM allids_with_timestamp) AS timestamps
                            ON alltime."jarTwoID"=timestamps.ids""")
    assert(createView == 0)
    println("Done")
  }

  private def createViewPrimaries(postgresInteractor: PostgresUtils): Unit = {
    println("Creating view on primary jars")
    val createView = postgresInteractor.runAndWait(
      sqlu"""
             CREATE OR REPLACE VIEW primary_data AS
                SELECT * FROM data WHERE classifier IS NULL""")
    assert(createView == 0)
    println("Done")
  }

  def writeCsvFile(fileName: String, header: Array[String], rows: List[Array[String]]): Try[Unit] = {
    println(s"Try to write csv $fileName")
    val csvwrite: CSVWriter = new CSVWriter(new FileWriter(fileName),
      ',',
      ICSVWriter.NO_QUOTE_CHARACTER,
      ICSVWriter.DEFAULT_ESCAPE_CHARACTER,
      ICSVWriter.DEFAULT_LINE_END)

    Try(csvwrite).flatMap((csvWriter: CSVWriter) =>
      Try{
        csvWriter.writeNext(header)
        csvWriter.writeAll(rows.asJava)
        csvWriter.close()
      } match {
        case f @ Failure(_) =>
          Try(csvWriter.close()).recoverWith{
            case _ => f
          }
        case success =>
          success
      }
    )
  }


  def shutdown(): Unit = {}
  def buildAnalysis(): Iterable[NamedAnalysis] = {Seq()}
}
