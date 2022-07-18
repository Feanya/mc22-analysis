package application

import analysis.{DeprecationAnalysis, NamedAnalysis}
import model._
import util.{DownloadLib, PostgresUtils}

import java.net.URL
import java.util.concurrent.TimeUnit

class PostgresApplication extends AnalysisApplication {
  var cfg: CliConfig = CliConfig()
  final protected val postgresInteractor = new PostgresUtils(cfg.environmentvariables)
  final protected val downloader = new DownloadLib()

  var pairResults: Iterable[PairResultDB] = List()
  var libraryResults: Iterable[LibraryResultDB] = List()

  override def main(args: Array[String]): Unit = {
    cfg = parseArguments(args)

    val oldStats = postgresInteractor.getDBStats

    var GAsWithNoJar: Int = 0
    var GAsWithOneJar: Int = 0

    if(cfg.cleanrun){
      profiler.start("Cleanup database")
      log.info("Cleaning up database for a fresh run ✨")
      postgresInteractor.cleanup()
    }

    profiler.start("Get sorted JarInfos (lazy)")
    // todo go on here
    lazy val chunks =
      for (i <- 0 until (cfg.limit/ cfg.chunksize))
        yield (i * cfg.chunksize)+cfg.offset

    var ga_list: Seq[(String, String)] = Seq()


    /**
     * Chunks ensure that the lists hold in memory stay small
     */
    chunks.foreach(chunk => {
      val jarInfosGAs: Seq[Seq[JarInfoDB]] = {
        if (cfg.library != "") {
          ga_list = postgresInteractor.getGAs(List(cfg.library))
        }
        else {
          ga_list = postgresInteractor.getGAs(offset=chunk, limit=cfg.chunksize)
          // debugging
          // println(ga_list.mkString("\n"))
        }
        // get library-coordinates from database
        ga_list
          // get URLS for all versions from database
          .map(ga => postgresInteractor.getJarInfoSemVerOnly(ga._1, ga._2))
          // sort them via comparison of SemVer-objects
          .map(_.sorted)
      }

      profiler.start("Filter jars")
      // just some counting for statistics
      jarInfosGAs.foreach(ga =>
        if (ga.isEmpty) GAsWithNoJar += 1
        else if (ga.length == 1) GAsWithOneJar += 1
      )

      // the actual filtering (only GAs with more than one interesting jar/version/GAV)
      val relevantGAsBySV = jarInfosGAs.filter(ga => ga.length > 1 && ga.length < 900)
      println(s"Got ${relevantGAsBySV.length} relevant GAs")

      if (!cfg.dryrun) {
        profiler.start("Analysis")
        downloadAndRunAnalyses(relevantGAsBySV)
      }

      val counts: Seq[Int] = relevantGAsBySV.map(_.length).sorted
      val mapCountsGA: Map[Int, Int] = counts.groupBy(identity).mapValues(_.size)

      val metaresults = Seq(
        MetaResultDB("GAsWithNoJar", GAsWithNoJar, ""),
        MetaResultDB("GAsWithOneJar", GAsWithOneJar , "")
      )
      postgresInteractor.insertMetaResults(metaresults)

      // Print end stuff
      log.info(s"✔ Done ")
      log.info(s"Looked with offset $chunk at \n" +
        s"$GAsWithNoJar GAs without Jars, \n$GAsWithOneJar GAs with one Jar\n" +
        s"${relevantGAsBySV.length} GAs with ${relevantGAsBySV.flatten.length} relevant versions: " +
        s"\n(#libraries, #versions)\n${mapCountsGA.toList.sortBy(_._1).map(_.swap).mkString("\t")}️")
    })

    // Reset for next run
    GAsWithNoJar = 0
    GAsWithOneJar = 0

    log.info("Old:" + oldStats.mkString(", "))
    val newStats = postgresInteractor.getDBStats
    log.info("New:" + newStats.mkString(", "))

    log.info("Changed:" + newStats.map(v => (v._1, v._2 - oldStats.getOrElse(v._1, 0))).mkString(", "))

    profiler.start("Pushing results")
    shutdown()
    val time = {TimeUnit.NANOSECONDS.toSeconds(profiler.stop().elapsedTime())}
    println(s"⚡️ This run took $time seconds. ⚡️")
    postgresInteractor.insertMetaResults(Seq(MetaResultDB("Runtime", time.toInt, "")))
    postgresInteractor.closeConnection()
    sys.exit()
  }

  private def downloadAndRunAnalyses(relevantGAsBySV: Iterable[Iterable[JarInfoDB]]): Unit = {
    val analyses: Seq[NamedAnalysis] = buildAnalysis()

    // urls_seq is two-dimensional: first dimension is of libraries…
    relevantGAsBySV.foreach(lib => {
      // download and analyse
      // …second dimension has all urls to the versions of one library
      lib.foreach(
        jarInfo =>
          try{
          downloader.downloadAndLoadOneURL(new URL(s"https://repo1.maven.org/maven2/${jarInfo.path}")) match {
            // run all analyses for one project per round
            case Some(project) =>
              this.handleResults(calculateResults(analyses, project, jarInfo))
            case None => log.error(s"Could not load ${jarInfo.path}")
          }
        }
        catch {
          case e: Exception =>
            val gav = s"${jarInfo.groupid}/${jarInfo.artifactname}/${jarInfo.version}"
            exceptionlog.log(gav, s"$gav: \n${e.getMessage}\n")
        }
      )
      // squash the library result to one iterable
      this.handleLibraryResults(analyses.flatMap(_.getLibraryResults))
      this.resetAnalyses(analyses)
    }
    )
  }

  override def handleResults(results: Iterable[PairResultDB]): Unit = {
    pairResults = pairResults ++ results
  }

  override def handleLibraryResults(results: Iterable[LibraryResultDB]): Unit = {
    libraryResults = libraryResults ++ results
  }

  override def shutdown(): Unit = {
    //println(pairResults.mkString("\n"))
    //println(libraryResults.mkString("\n"))
    postgresInteractor.insertPairResults(pairResults)
    postgresInteractor.insertLibraryResults(libraryResults)
  }

  /**
   * Returns the chosen analyses, initialized, as a sequence to be worked with.
   *
   * @return Sequence of initalized analyses to be conducted
   */
  def buildAnalysis(): Seq[NamedAnalysis] = {
    val analyses = Seq(
      new DeprecationAnalysis()
      //, new SizeAnalysis()
    )
    analyses.foreach(_.initialize())
    if (cfg.verbose) {
      analyses.foreach(_.setVerbose())
    }
    analyses
  }
}
