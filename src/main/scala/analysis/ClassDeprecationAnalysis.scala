package analysis

import just.semver.SemVer
import model.LibraryResult
import org.opalj.br.analyses.Project

import java.net.URL
import scala.util.Try
import scala.util.matching.Regex
import util.SemVerUtils

/**
 * based on impl.group1.EvolutionAnalysis
 *
 * @param jarDir directory containing the different jar-file vs of a software to analyze
 *               Optional CLI arguments:
 */
class ClassDeprecationAnalysis() extends NamedAnalysis {
  val SVU = new SemVerUtils()
  val dummySemVer: SemVer = SVU.dummySemVer

  var previousJar: String = ""
  var currentJar: String = ""
  var previousVersion: SemVer = dummySemVer
  var currentVersion: SemVer = dummySemVer

  var previousClasses: scala.collection.Set[String] = Set()
  var previousDepr: scala.collection.Set[String] = Set()

  // library info
  var allDepr: scala.collection.Set[String] = Set()
  var groupid: String = ""
  var artifactname: String = ""

  var roundCounter: Integer = 0


  /**
   * Prepare all variables
   */
  override def initialize(): Unit = {
    previousJar = ""
    currentJar = ""
    roundCounter = 0
    previousVersion = dummySemVer
    currentVersion = dummySemVer
    previousClasses = Set()
    previousDepr = Set()
  }


  /**
   * @param project Fully initialized OPAL project representing the JAR file under analysis
   * @return Try[T] object holding the intermediate result, if successful
   *         Try[T] = Try[(Double)]
   *         String: entityIdent
   */
  def produceAnalysisResultForJAR(project: Project[URL], jarname: String, version: String): Try[Double] = {
    currentJar = jarname

    // Get the fully qualified names (fqn) of all classes in a set
    val currentClasses: Set[String] = project.allProjectClassFiles.map(_.fqn).toSet

    // Find deprecation tags in classes for next round
    val deprecationPattern: Regex = "java/lang/Deprecated".r
    var deprecatedClasses: Set[String] = Set()

    // todo
    // nice idea to remove all the regex-matching, but class-info files do not get detected and therefore
    // important deprecations get lost
    project.allClassFiles.foreach(
      cl => {
        if (cl.isDeprecated) {
          deprecatedClasses += cl.fqn
        }
        if (cl.fqn.split("/").takeRight(1)(0) == "package-info") {
          log.error("package-info found")
        }
      }
    )

    currentVersion = SVU.parseSemVerString(version)

    if (roundCounter > 0) {
      log.warn(s"Calculating class-differences between: \n" +
        s"$previousJar ($previousVersion) and \n" +
        s"$currentJar ($currentVersion)… \n(${SVU.calculateVersionjump(previousVersion, currentVersion)})")
      val allClasses = currentClasses.union(previousClasses)
      val maintainedClasses = currentClasses.intersect(previousClasses)

      // Find the new classes
      val newClasses = currentClasses.diff(previousClasses)
      val removedClasses = previousClasses.diff(currentClasses)

      // Find removals
      val deprAndRemovedClasses = removedClasses.intersect(previousDepr)
      val deprNotRemovedClasses = previousDepr.diff(removedClasses)
      val removedNotDeprClasses = removedClasses.diff(previousDepr)

      // Library analysis
      allDepr = allDepr.union(deprecatedClasses)

      // Print stats
      log.info(s"All classes:        ${allClasses.size}")
      log.info(s"maintained classes: ${maintainedClasses.size}")
      log.info(s"Added classes:      ➕${newClasses.size}")
      log.info(s"Removed classes:    ➖${removedClasses.size}")

      log.info(s"🗑 Deprecations️ overall: ${allDepr.size}")
      log.info(s"🗑 Deprecations️ in version A: ${previousDepr.size}")
      log.info(s"🗑 Deprecated in A and removed in B ✔️: ${deprAndRemovedClasses.size}")
      log.info(s"🗑 Deprecated but not removed ❌: ${deprNotRemovedClasses.size}")
      log.debug(deprNotRemovedClasses.take(10).mkString("\n"))
      log.info(s"❌ Removed but not deprecated ❌: ${removedNotDeprClasses.size}")
      log.debug(removedNotDeprClasses.take(10).mkString("\n"))

    } else {
      log.info(s"Initial round on $currentJar")
    }

    // prepare for next round
    previousJar = currentJar
    previousClasses = currentClasses
    previousDepr = deprecatedClasses
    previousVersion = currentVersion
    roundCounter += 1

    // return result
    Try(0)
  }


  /**
   * The name for this analysis implementation. Will be used to include and exclude analyses via CLI.
   */
  override def analysisName: String = "ClassDeprecation"

  override def getLibraryResults(): Iterable[LibraryResult] = {
    List(analysisName,  allDepr.size)
  }
}