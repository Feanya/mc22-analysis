package analysis

import model._
import org.opalj.br.analyses.Project
import util.SemVerUtils

import java.net.URL

/**
 * Determine the overall and average sizes of the relevant primary artifacts per GA/library
 */
class SizeAnalysis() extends NamedAnalysis {
  val SVU = new SemVerUtils()

  var previousJarInfoDB: JarInfoDB = JarInfoDB.initial
  var currentJarInfoDB: JarInfoDB = JarInfoDB.initial

  // library info
  var totalSize: Double = 0

  var roundCounter: Int = 0


  /**
   * Prepare all variables
   */
  override def initialize(): Unit = {
    previousJarInfoDB = JarInfoDB.initial
    currentJarInfoDB = JarInfoDB.initial

    totalSize = 0   // in bytes, btw

    roundCounter = 0
  }

  def setVerbose(): Unit = {}


  /**
   * @param project Fully initialized OPAL project representing the JAR file under analysis
   * @return Try[T] object holding the intermediate result, if successful
   *         Try[T] = Try[(Double)]
   *         String: entityIdent
   */
  def produceAnalysisResultForJAR(project: Project[URL], JarInfoDB: JarInfoDB): Option[Seq[PairResultDB]] = {
    currentJarInfoDB = JarInfoDB

    var result: Option[Seq[PairResultDB]] = None


    if (roundCounter > 0) {

      val sizeChange: Double = currentJarInfoDB.size-previousJarInfoDB.size

      log.debug(s"Size-difference between: \n" +
        s"${previousJarInfoDB.jarname} and ${currentJarInfoDB.jarname}… : $sizeChange \n")

      val results = Seq(("SizeDifference", sizeChange))

      val versionjump: Int = SVU.calculateVersionjump(previousJarInfoDB.version, currentJarInfoDB.version).toInt
      result = Some(results.map(r => PairResultDB(r._1, previousJarInfoDB.id, currentJarInfoDB.id, versionjump, r._2)))
    }
    // First round, no calculations
    else {
      log.debug(s"Initial round on ${currentJarInfoDB.jarname}")
      result = Some(Seq())
    }

    // Library analysis
    totalSize += currentJarInfoDB.size

    // prepare for next round
    previousJarInfoDB = currentJarInfoDB
    roundCounter += 1

    // return result
    result
  }

  override def getLibraryResults: Iterable[LibraryResultDB] = {
    val g = currentJarInfoDB.groupid
    val a = currentJarInfoDB.artifactname
    Seq(LibraryResultDB("TotalSize", g, a, totalSize),
        LibraryResultDB("AverageSize", g, a, (totalSize/(roundCounter+1))))
  }

  /**
   * The name for this analysis implementation. Will be used to include and exclude analyses via CLI.
   */
  override def analysisName: String = "SizeAnalysis"
}