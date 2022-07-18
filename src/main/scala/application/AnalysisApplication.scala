package application

import analysis.NamedAnalysis
import model._
import org.opalj.br.analyses.Project
import org.sellmerfud.optparse._
import org.slf4j.profiler.Profiler
import org.slf4j.{Logger, LoggerFactory}
import util.ExceptionLogger

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

  protected final val exceptionlog: ExceptionLogger = new ExceptionLogger("app")

  // todo set log level to debug

  protected final val profiler: Profiler = new Profiler("Analysis-Application basic")

  def parseArguments(args: Array[String]): CliConfig = {
    val config = try {
      new OptionParser[CliConfig] {
        separator("Flags:")
        flag("-c", "--clean", "Remove old results before starting analysis.") {
          _.copy(cleanrun = true)
        }
        flag("-d", "--dryrun", "Do not download jars and conduct analyses.") {
          _.copy(dryrun = true)
        }
        flag("-e", "--env", "Read environment variables for postgres-credentials.") {
          _.copy(environmentvariables = true)
        }
        flag("-v", "--verbose", "Write more things to stderr") {
          _.copy(verbose = true)
        }

        separator("")
        separator("Options:")

        optl[String]("-g <ga-coordinate>", "--library",
          "Enter specific library/GA to analyse in the form of <G:A>, e.g.: <org.mockito:mockito-core>.")
          { (v, c) => c.copy(library = v getOrElse "") }

        optl[Int]("-o <offset>", "--offset", "Enter offset of libraries/GA.") { (v, c) => c.copy(offset = v getOrElse 0) }

        optl[Int]("-l <limit>", "--limit", "Enter limit of libraries/GA to pull.") { (v, c) => c.copy(limit = v getOrElse 1) }

        optl[Int]("-s <chunksize>", "--size",
          "Enter limit of libraries/GA to work through in one chunk.") { (v, c) => c.copy(chunksize = v getOrElse 10) }

        // reqd[String]("-t", "--type=<type>", List("ascii", "binary"), "Set the data type. (ascii, binary)") { (v, c) => c.copy(fileType = v) }

      }.parse(args, CliConfig())
    }
    catch {
      case e: OptionParserException => log.error(e.getMessage); sys.exit(1)
    }

    log.info("Config: " + config)
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
  def calculateResults(analyses: Iterable[NamedAnalysis], project: Project[URL], jarInfo: JarInfoDB): Iterable[PairResultDB] =
    analyses.flatMap(analysis =>
      analysis.produceAnalysisResultForJAR(project, jarInfo) match {
        case Some(result) => result
        // todo solve this more elegantly?
        case None => log.error(s"Analysis '${analysis.analysisName}' failed for ${jarInfo.jarname}"); Seq()
      }
    )

  /**
   * Prints results to the CLI
   *
   * @param results Results to process
   */
  def handleResults(results: Iterable[PairResultDB]): Unit = {
    results.foreach { result =>
      println(s"Result for '${result.resultname}' " +
        s"on\n${result.jarOneID}/${result.jarTwoID} (${result.versionjump}): ${result.value}")
    }
  }

  /**
   * Handle results of library analyses
   * */
  def handleLibraryResults(results: Iterable[LibraryResultDB]): Unit = {
    println(results.mkString("\n"))
  }


  /** Do all the things left to be done
   **/
  def shutdown(): Unit

  /**
   * The main entrypoint for every analysis application. Parses CLI input and controls the application lifecycle.
   *
   * @param arguments List of arguments
   */
  def main(arguments: Array[String]): Unit = {
    log.info("Up down strange charm ✨ Please implement main()-method in your application, thank you!")
  }

  case class CliConfig(cleanrun: Boolean = false,
                       dryrun: Boolean = false,
                       environmentvariables: Boolean = false,
                       verbose: Boolean = false,
                       library: String = "",
                       offset: Int = 0,
                       limit: Int = 5,
                       chunksize: Int = 20)

}