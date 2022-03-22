package application

import analysis.{ClassDeprecationAnalysis, NamedAnalysis}
import util.{DownloadLib, PostgresUtils}

import java.net.URL

class PostgresApplication extends AnalysisApplication {

  /**
   * Returns the chosen analyses, initialized, as a sequence to be worked with.
   *
   * @return Sequence of analyses to be conducted */
  def buildAnalysis(): Seq[NamedAnalysis] = {
    val analyses = Seq(new ClassDeprecationAnalysis())
    analyses.foreach(_.initialize())
    analyses
  }

}

object PostgresApplicationObject extends PostgresApplication {
  val analyses: Seq[NamedAnalysis] = buildAnalysis()

  val postgresInteractor = new PostgresUtils()
  val urls_seq: Seq[Seq[URL]] = {
    // get library-coordinates from database
    postgresInteractor.getGAs(5)
    // get URLS for all versions from database
    .map(ga => postgresInteractor.getURLsAllVersions(ga._1, ga._2))
  }

  val downloader = new DownloadLib()
  urls_seq.foreach(lib =>
    // download and analyse
    lib.foreach(url => downloader.downloadAndLoadOne(url) match {
      // run all analyses for one project per round
      case Some(project) => calculateResults(analyses, project, url)
      case None => log.error(s"Could not load $url")
  }))
}
