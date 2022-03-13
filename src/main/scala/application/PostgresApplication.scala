package application

import analysis.{ClassDeprecationAnalysis, NamedAnalysis}
import util.{DownloadLib, PostgresUtils}

class PostgresApplication extends AnalysisApplication {

  /**
   * Method that executes all analyses on the input file(s) and produces the resulting List of JarFileMetrics.
   * * @return Tuple containing 1) List of JarFileMetricsResults and 2) the ApplicationPerformanceStatistics
   */
  def calculateResults(): Unit = {

  }

  override def buildAnalysis(): Seq[NamedAnalysis] = {
    Seq(
      new ClassDeprecationAnalysis()
    )
  }

}

object PostgresApplicationObject extends PostgresApplication {
  val classDeprecationAnalysis = buildAnalysis().head

  val postgresInteractor = new PostgresUtils()
  val gas: Seq[(String, String)] = postgresInteractor.getGAs(5)
  val urls_seq = gas.map(ga => postgresInteractor.getURLsAllVersions(ga._1, ga._2))

  val downloader = new DownloadLib()
  urls_seq.foreach(
    lib =>
      lib.foreach(url =>
        downloader.downloadAndLoadOne(url) match {
          case Some(project) =>
            classDeprecationAnalysis.produceAnalysisResultForJAR(project, url.toString.split("/").takeRight(1).head)
          case None => log.error(s"Could not download $url")
        }
      )
  )
}
