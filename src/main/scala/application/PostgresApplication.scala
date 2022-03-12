package application

import analysis.{ClassDeprecationAnalysis, NamedAnalysis}
import util.DownloadLib
import util.PostgresUtils

import java.net.URL

class PostgresApplication extends AnalysisApplication {

  /**
   * Method that executes all analyses on the input file(s) and produces the resulting List of JarFileMetrics.
   * * @return Tuple containing 1) List of JarFileMetricsResults and 2) the ApplicationPerformanceStatistics
   */
  def calculateResults(): Unit = {

  }


  override def buildAnalysis(): Seq[NamedAnalysis] = {Seq(
    new ClassDeprecationAnalysis()
  )
  }

}


object PostgresApplicationObject extends PostgresApplication {
  val cda = buildAnalysis().head

  val postgresInteractor = new PostgresUtils()
  val urls: Seq[URL] = postgresInteractor.getURLsAllVersions("junit", "junit")

  val downloader = new DownloadLib()

  urls.foreach(url =>
    downloader.downloadAndLoadOne(url) match{
      case Some(project) => cda.produceAnalysisResultForJAR(project)
      case None => log.error("heeeelp")
    }

  )


}
