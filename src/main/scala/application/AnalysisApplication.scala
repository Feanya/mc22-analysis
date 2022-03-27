package application

import analysis.NamedAnalysis
import model.{JarInfo, LibraryResult, PairResult}
import org.opalj.br.analyses.Project
import org.sellmerfud.optparse._
import org.slf4j.profiler.Profiler
import org.slf4j.{Logger, LoggerFactory}

import java.net.URL


/**
 * Base trait for applications that execute any kind of analysis on JAR files. Provides Lifecycle Hooks,
 * export functionality and logging access.
 */
trait AnalysisApplication {

  /**
   * The logger for this instance
   */
  protected final val log: Logger = LoggerFactory.getLogger(this.getClass)
  // todo set log level to debug

  protected final val profiler: Profiler = new Profiler("Analysis-Application basic")

  def parseArguments(args: Array[String]): CliConfig = {
    val config = try {
      new OptionParser[CliConfig] {
        separator("Flags:")
        flag("-d", "--dryrun", "Do not download jars and conduct analyses.") { _.copy(dryrun = true) }

        separator("")
        separator("Options:")

        optl[Int]("-o <offset>", "", "Enter offset of libraries/GA.") { (v, c) => c.copy(offset = v getOrElse 0) }

        optl[Int]("-l <limit>", "", "Enter limit of libraries/GA to pull.") { (v, c) => c.copy(limit = v getOrElse 1) }

        // reqd[String]("-t", "--type=<type>", List("ascii", "binary"), "Set the data type. (ascii, binary)") { (v, c) => c.copy(fileType = v) }

      }.parse(args, CliConfig())
    }
    catch {
      case e: OptionParserException => println(e.getMessage); sys.exit(1)
    }

    log.info("config: " + config)
    config
  }

  /**
   * to be implemented by the application: collect all analyses which shall be conducted
   *
   * @return sequence of analyses to call
   */
  def buildAnalysis(): Iterable[NamedAnalysis]

  /**
   * Reset all analyses after e.g. processing one library
   *
   * @param analyses analyses to conduct
   */
  def resetAnalyses(analyses: Iterable[NamedAnalysis]): Unit = analyses.foreach(_.reset())

  /**
   * Method that executes all analyses on the input file(s) and produces the resulting List of JarFileMetrics.
   * * @return PairResults from analysis of one jar (and its predecessor) */
  def calculateResults(analyses: Iterable[NamedAnalysis], project: Project[URL], jarInfo: JarInfo): Iterable[PairResult] =
    analyses.map(analysis =>
      analysis.produceAnalysisResultForJAR(project, jarInfo) match {
        case Some(result) => result
        case None => PairResult.analysisFailed(analysis.analysisName, jarInfo, jarInfo)
      }
    )

  /**
   * Prints results to the CLI and writes them to the database
   *
   * @param results Results to process
   */
  def handleResults(results: Iterable[PairResult]): Unit = {
    // todo write result in database with foreign case

    results.foreach { result =>
      log.info(s"Results for '${result.analysisName}' " +
        s"on\n${result.jarOneInfo.jarname}/${result.jarTwoInfo.jarname} (${result.versionjump}):")
      result.results.foreach { v =>
        log.info(s"\t${v.name}: ${v.value}")
      }
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

  case class CliConfig(dryrun: Boolean = false,
                       offset: Int = 0,
                       limit: Int = 1)

}