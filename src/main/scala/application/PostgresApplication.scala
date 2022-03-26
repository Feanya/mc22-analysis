package application

import analysis.{ClassDeprecationAnalysis, NamedAnalysis}
import model.JarInfo
import util.{DownloadLib, PostgresUtils}

class PostgresApplication extends AnalysisApplication {
  override def main(arguments: Array[String]): Unit = {
    val dryrun: Boolean = false
    val limit: Int = 25
    val analyses: Seq[NamedAnalysis] = buildAnalysis()

    profiler.start("Get sorted JarInfos")
    val postgresInteractor = new PostgresUtils()
    val jarInfosGAs: Seq[Seq[JarInfo]] = {
      // get library-coordinates from database
      postgresInteractor.getGAs(limit)
        // get URLS for all versions from database
        .map(ga => postgresInteractor.getJarInfoSemVerOnly(ga._1, ga._2))
        .map(_.sorted)
    }

    // sort
    profiler.start("Sort and filter URLs")
    val urlsVersionsSortedBySV = urlsVersionsSortedByTimestamp.map(_.sortWith(_._2 < _._2))
    urlsVersionsSortedBySV.foreach(ga =>
      if(ga.isEmpty) log.warn(s"Empty GA: ${ga}")
      else if(ga.length == 1) {log.debug(s"GA with one version: ${ga}")}
      )
    val relevantGAsBySV = urlsVersionsSortedBySV.filter(ga => ga.length > 1)

    if (!dryrun) {
      profiler.start("Analysis")
      val downloader = new DownloadLib()
      // urls_seq is two-dimensional: first dimension is of libraries…
      relevantGAsBySV.foreach(lib => {
        // download and analyse
        // …second dimension has all urls to the versions of one library
        lib.foreach(jarinfo =>
          downloader.downloadAndLoadOne(jarinfo.url) match {
            // run all analyses for one project per round
            case Some(project) =>
              this.handleResults(calculateResults(analyses, project, jarinfo))
            case None => log.error(s"Could not load ${jarinfo.url}")
          })
        this.resetAnalyses(analyses)
      }
      )
    }

    val c = relevantGAsBySV.map(_.length).sorted
    val m = c.groupBy(identity).mapValues(_.size)

    log.info(counts.mkString(", "))
    log.info(s"✔ Done: ${analyses.map(_.analysisName).mkString(", ")}!")
    log.info(s"Looked with limit ${limit} at \n" +
      s"${GAsWithNoJar} GAs without Jars, \n${GAsWithOneJar} GAs with one Jar\n" +
      s"${counts.length} GAs with ${relevantGAsBySV.flatten.length} relevant versions: " +
      s"\n(#libraries, #versions)\n${mapCountsGA.toList.sortBy(_._1).map(_.swap).mkString("\n")}️")

    profiler.stop().print()
  }

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
