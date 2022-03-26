package application

import analysis.NamedAnalysis
import model.{JarInfo, LibraryResult, PairResult}
import org.opalj.br.analyses.Project
import org.slf4j.{Logger, LoggerFactory}
import output.CsvFileOutput

import java.net.URL
import org.slf4j.profiler.Profiler

/**
 * Base trait for applications that execute any kind of analysis on JAR files. Provides Lifecycle Hooks,
 * export functionality and logging access.
 */
trait AnalysisApplication extends CsvFileOutput {

  /**
   * The logger for this instance
   */
  protected final val log: Logger = LoggerFactory.getLogger(this.getClass)
  // todo set log level to debug

  protected final val profiler: Profiler = new Profiler("Analysis-Application basic")


  /**
   * to be implemented by the application: collect all analyses which shall be conducted
   *
   * @return sequence of analyses to call
   */
  def buildAnalysis(): Seq[NamedAnalysis]

  /**
   * Reset all analyses after e.g. processing one library
   *
   * @param analyses analyses to conduct
   */
  def resetAnalyses(analyses: Seq[NamedAnalysis]): Unit = analyses.foreach(_.reset())

  /**
   * Method that executes all analyses on the input file(s) and produces the resulting List of JarFileMetrics.
   * * @return Tuple containing 1) List of JarFileMetricsResults and 2) the ApplicationPerformanceStatistics
   * (List[PairResult], ApplicationPerformanceStatistics) */
  def calculateResults(analyses: Seq[NamedAnalysis], project: Project[URL], jarInfo: JarInfo): Seq[PairResult] =
  analyses.map(analysis =>
    analysis.produceAnalysisResultForJAR(project, jarInfo) match{
      case Some(result) => result
      case None => PairResult.analysisFailed(analysis.analysisName, jarInfo, jarInfo)
    }
  )

  /**
   * Prints results to the CLI and writes them to the database
   *
   * @param results Results to process
   */
  def handleResults(results: List[PairResult]): Unit = {
    // todo write result in database with foreign case



    // todo: add output file
    /*if (appConfiguration.outFileOption.isDefined) {
      log.info(s"Writing results to output file ${appConfiguration.outFileOption.get}")
      writeResultsToFile(appConfiguration.outFileOption.get, results) match {
        case Failure(ex) =>
          log.error("Error writing results", ex)
        case Success(_) =>
          log.info(s"Done writing results to file")
      }*/

    results.foreach { res =>
      log.info(s"Results for analysis '${res.analysisName}' on ${res.jarFileOne.getName}/${res.jarFileTwo.getName}:")
      res.results.foreach { v =>
        log.info(s"\t- ${v.metricName} on ${v.jarNameOne}→${v.jarNameTwo}: ${v.value}")
      }
    }

  /**
   * Write results of library analyses to the database
   * */
  def handleLibraryResults(results: Iterable[LibraryResult]): Unit = {
    println(results.mkString("\n"))
  }

  /**
   * The main entrypoint for every analysis application. Parses CLI input and controls the application lifecycle.
   *
   * @param arguments List of arguments
   */
  def main(arguments: Array[String]): Unit = {
    log.info("Up down strange charm ✨ Please implement main()-method in your application, thank you!")
  }

}