package analysis

import just.semver.SemVer
import model._
import org.opalj.br.{ClassFile, Method}
import org.opalj.br.analyses.Project
import util.SemVerUtils

import java.net.URL
import scala.language.postfixOps
import scala.util.matching.Regex


/**
 * based on impl.group1.EvolutionAnalysis
 */
class DeprecationAnalysis() extends NamedAnalysis {
  val SVU = new SemVerUtils()
  val dummySemVer: SemVer = SVU.dummySemVer
  var verbose = false

  var previousJarInfoDB: JarInfoDB = JarInfoDB.initial
  var currentJarInfoDB: JarInfoDB = JarInfoDB.initial

  var currentClassesWithAccessFilter: Iterable[ClassFile] = Iterable()
  var currentMethodsWithAccessFilter: Iterable[Method] = Iterable()

  var previousPubMethods: Set[String] = Set()
  var previousProtMethods: Set[String] = Set()
  var previousPubMDepr: Set[String] = Set()
  var previousProtMDepr: Set[String] = Set()

  var previousClasses: Set[String] = Set()
  var previousCDepr: Set[String] = Set()

  // library info
  var allCDepr: Set[String] = Set()
  var allPubMDepr: Set[String] = Set()
  var allProtMDepr: Set[String] = Set()
  var roundCounter: Integer = 0

  var wronglyRemovedMethods: Int = 0
  var wronglyRemovedClasses: Int = 0

  /**
   * Prepare all variables
   */
  override def initialize(): Unit = {
    previousJarInfoDB = JarInfoDB.initial
    currentJarInfoDB = JarInfoDB.initial

    previousClasses = Set()
    previousCDepr = Set()
    previousPubMethods = Set()
    previousProtMethods = Set()
    previousPubMDepr = Set()
    previousProtMDepr = Set()

    // library stuff
    allCDepr = Set()
    allPubMDepr = Set()
    allProtMDepr = Set()
    wronglyRemovedMethods = 0
    wronglyRemovedClasses = 0
    roundCounter = 0
  }

  def setVerbose(): Unit = {
    verbose = true
  }

  /**
   * @param project Fully initialized OPAL project representing the JAR file under analysis
   * @return Try[T] object holding the intermediate result, if successful
   *         Try[T] = Try[(Double)]
   *         String: entityIdent
   */
  def produceAnalysisResultForJAR(project: Project[URL], JarInfoDB: JarInfoDB): Option[Seq[PairResultDB]] = {
    currentJarInfoDB = JarInfoDB
    var result: Option[Seq[PairResultDB]] = None

    // Note: A class file cannot have private or protected visibility.
    currentClassesWithAccessFilter = project.allClassFiles.filter(_.isPublic)
    // Get the fully qualified names (fqn) of all classes in a set
    val currentClasses: Set[String] = currentClassesWithAccessFilter
      .map(cl => s"${cl.fqn}")
      .toSet

    currentMethodsWithAccessFilter = project.allMethods.filter(m => m.isPublic || m.isProtected)
    val currPubM = currentMethodsWithAccessFilter
      .filter(_.isPublic)
      .map(m => m.classFile.fqn + ":" + m.name)
      .toSet
    val currProtM = currentMethodsWithAccessFilter
      .filter(_.isProtected)
      .map(m => m.classFile.fqn + ":" + m.name)
      .toSet

    if (verbose) {
      log.info("Number of classes:" + currentClasses.size)
      log.info(s"Number of methods: ${currentMethodsWithAccessFilter.size} = ${currPubM.size} + ${currProtM.size}")
    }


    // Find deprecation tags in classes for next round
    val deprecations = findDeprecations(project, currentClasses)
    val deprecatedClasses = deprecations._1
    val deprecatedPubMethods = deprecations._2
    val deprecatedProtMethods = deprecations._3

    if (roundCounter > 0) {
      val resultsClasses = calculateClassResults(currentClasses)
      val resultsMethods = calculateMethodResults(currPubM, currProtM)

      val results = resultsClasses ++ resultsMethods

      val versionjump: Int = SVU.calculateVersionjump(previousJarInfoDB.version, currentJarInfoDB.version).toInt
      result = Some(results.map(r => PairResultDB(r._1, previousJarInfoDB.id, JarInfoDB.id, versionjump, r._2)))
    }
    // First round, no calculations
    else {
      log.debug(s"Initial round on ${currentJarInfoDB.jarname}")
      result = Some(Seq())
    }

    // Library analysis: collect all deprecations
    allCDepr = allCDepr.union(deprecatedClasses)
    allPubMDepr = allPubMDepr.union(deprecatedPubMethods)
    allProtMDepr = allProtMDepr.union(deprecatedProtMethods)

    // prepare for next round
    previousJarInfoDB = currentJarInfoDB
    previousClasses = currentClasses
    previousCDepr = deprecatedClasses

    previousPubMethods = currPubM
    previousProtMethods = currProtM
    previousPubMDepr = deprecatedPubMethods
    previousProtMDepr = deprecatedProtMethods

    roundCounter += 1

    // Just for debugging purposes.
    //if(roundCounter == 5) {sys.exit()}

    result
  }


  // todo fix issue with missing prot deprecations
  private def calculateMethodResults(curPubMethods: Set[String], curProtMethods: Set[String]): Seq[(String, Int)] = {
    log.debug(s"Calculating M-differences between: \n" +
      s"${previousJarInfoDB.jarname} and \n" +
      s"${currentJarInfoDB.jarname}… \n" +
      s"(${SVU.calculateVersionjump(previousJarInfoDB.version, currentJarInfoDB.version)})")
    val combinedPubM = curPubMethods.union(previousPubMethods)
    val combinedProtM = curProtMethods.union(previousProtMethods)

    val maintainedPubM = curPubMethods.intersect(previousPubMethods)
    val maintainedProtM = curProtMethods.intersect(previousProtMethods)

    // Find the new entities
    val newPubM = curPubMethods.diff(previousPubMethods)
    val newProtM = curProtMethods.diff(previousProtMethods)

    // Find removals
    val removedPubM = previousPubMethods.diff(curPubMethods)
    val removedProtM = previousProtMethods.diff(curProtMethods)

    // Find removal/deprecations /todo this is the critical part
    val deprAndRemovedPubM  = removedPubM.intersect(previousPubMDepr)
    val deprAndRemovedProtM = removedProtM.intersect(previousProtMDepr)
    val deprNotRemovedPubM = previousPubMDepr.diff(removedPubM)
    val deprNotRemovedProtM = previousProtMDepr.diff(removedProtM)
    val removedNotDeprPubM = removedPubM.diff(previousPubMDepr)
    val removedNotDeprProtM = removedProtM.diff(previousProtMDepr)

    // Print stats
    val wronglyRemovedCount: Int = removedNotDeprPubM.size + removedNotDeprProtM.size
    if (verbose) { // && wronglyRemovedCount > 0
      log.info(s"************************************************")
      log.info(s"All M:        📢 ${combinedPubM.size}  , 🛡️ ${combinedProtM.size}")
      log.info(s"Maintained M: 📢 ${maintainedPubM.size}  , 🛡️  ${maintainedProtM.size}")
      log.info(s"Added M:      ➕ 📢 ${newPubM.size}  , 🛡️  ${newProtM.size}")
      log.info(s"Removed M:    ➖ 📢 ${removedPubM.size}  , 🛡️  ${removedProtM.size}")

      log.info(s"🗑 MDeprecations️ in version A:          📢 ${previousPubMDepr.size} , 🛡️ ${previousProtMDepr.size}")
      log.info(s"🗑 MDeprecated in A and removed in B ✔️: 📢 ${deprAndRemovedPubM.size} , 🛡️ ${deprAndRemovedProtM.size}")
      log.info(s"🗑 MDeprecated but not removed ❌:       📢 ${deprNotRemovedPubM.size} , 🛡️ ${deprNotRemovedProtM.size}")
      log.warn(s"❌ MRemoved but not deprecated ❌:        $wronglyRemovedCount ( 📢 ${removedNotDeprPubM.size}/ 🛡️ ${removedNotDeprProtM.size})")

      //log.info(removedNotDeprProtM.take(5).mkString("\n❌❌ Missing \uD83D\uDEE1 protected: "))
      //log.info(removedNotDeprPubM.take(100).mkString("\n❌❌ Missing \uD83D\uDCE2 public: "))
      //log.info(removedNotDeprE.take(10).map(e => e.split(":").head).mkString("\n❌❌method in: "))
      log.info(s"************************************************")
    }

    // debugging purposes
    // if(deprAndRemovedProtM.size > 1) {sys.exit()}

    // for library analysis
    wronglyRemovedMethods += wronglyRemovedCount

    Seq(
      (s"MDeprecatedInPrevProt", previousPubMDepr.size),
      (s"MDeprecatedInPrevPub", previousProtMDepr.size),
      (s"MDeprecatedAndRemovedPub", deprAndRemovedPubM.size),
      (s"MDeprecatedAndRemovedProt", deprAndRemovedProtM.size),
      (s"MDeprecatedNotRemovedProt", deprNotRemovedProtM.size),
      (s"MDeprecatedNotRemovedPub", deprNotRemovedPubM.size),
      (s"MWronglyRemovedProt", removedNotDeprProtM.size),
      (s"MWronglyRemovedPub", removedNotDeprPubM.size))
  }

  private def calculateClassResults(currentEntities: Set[String]): Seq[(String, Int)] = {
    log.debug(s"Calculating class-differences between: \n" +
      s"${previousJarInfoDB.jarname} and \n" +
      s"${currentJarInfoDB.jarname}… \n" +
      s"(${SVU.calculateVersionjump(previousJarInfoDB.version, currentJarInfoDB.version)})")
    val combinedE = currentEntities.union(previousClasses)
    val maintainedE = currentEntities.intersect(previousClasses)

    // Find the new entities
    val newE = currentEntities.diff(previousClasses)
    val removedE = previousClasses.diff(currentEntities)

    // Find removals
    val deprAndRemovedE = removedE.intersect(previousCDepr)
    val deprNotRemovedE = previousCDepr.diff(removedE)
    val removedNotDeprE = removedE.diff(previousCDepr)

    // Print stats
    val wronglyRemovedCount: Int = removedNotDeprE.size
    if (verbose) {
      log.info(s"************************************************")
      log.info(s"All classes:        ${combinedE.size}")
      log.info(s"Maintained classes: ${maintainedE.size}")
      log.info(s"Added classes:      ➕${newE.size}")
      log.info(s"Removed classes:    ➖${removedE.size}")

      log.info(s"🗑 CDeprecations️ in version A:          ${previousCDepr.size}")
      log.info(s"🗑 CDeprecated in A and removed in B ✔️: ${deprAndRemovedE.size}")
      log.info(s"🗑 CDeprecated but not removed ❌:       ${deprNotRemovedE.size}")
      log.warn(s"❌ CRemoved but not deprecated ❌:        $wronglyRemovedCount")

      //log.info(removedNotDeprE.take(100).mkString("\n❌ Missing class: "))
      //log.info(removedNotDeprE.take(10).map(e => e.split(":").head).mkString("\n❌❌method in: "))
      log.info(s"************************************************")
    }

    // for library analysis
    wronglyRemovedClasses += wronglyRemovedCount

    // results
    Seq(
      (s"CDeprecatedInPrev", previousCDepr.size),
      (s"CDeprecatedAndRemoved", deprAndRemovedE.size),
      (s"CDeprecatedNotRemoved", deprNotRemovedE.size),
      (s"CWronglyRemoved", removedNotDeprE.size)
    )
  }

  private def findDeprecations(project: Project[URL], currentClasses: Set[String]):
  (Set[String], Set[String], Set[String]) = {
    // Easy hits: simply marked as deprecated
    var deprecatedClasses: Set[String] = project.allClassFiles
      .filter(_.isDeprecated)
      .map(_.fqn).toSet
    val deprecatedPubMethods: Set[String] = currentMethodsWithAccessFilter
      .filter(_.isDeprecated)
      .filter(_.isPublic)
      .map(m => s"${m.classFile.fqn}:${m.name}").toSet
    val deprecatedProtMethods: Set[String] = currentMethodsWithAccessFilter
      .filter(_.isDeprecated)
      .filter(_.isProtected)
      .map(m => s"${m.classFile.fqn}:${m.name}").toSet

    if(verbose){
      log.info("Lvl 1 class-hits: " + deprecatedClasses.size.toString)
      log.info(s"Lvl 1 method-hits: 📢 ${deprecatedPubMethods.size} / 🛡️ ${deprecatedProtMethods.size}")
    }

    // todo

    {
      // Not so easy hits: the package is marked as deprecated
      val deprecationPattern: Regex = ".*Deprecated".r

      // probably not the most efficient way to do this, but at least comprehensible
      val packageInfos = project.allClassFiles.filter(cl => cl.fqn.split("/").takeRight(1)(0) == "package-info")
      packageInfos.foreach({ cl =>
        log.warn("package-info found: " + cl.fqn)
        deprecationPattern.findFirstMatchIn(cl.annotations.mkString(",")) match {
          case Some(_) =>
            val packagename = cl.fqn.stripSuffix("/package-info")
            log.warn(s"$packagename is marked as deprecated")
            val packagedeprecatedClassFqns = currentClasses.filter(_.contains(packagename))

            // actually insert into deprecation set
            deprecatedClasses = deprecatedClasses.union(packagedeprecatedClassFqns)

            // just some debugging
            packagedeprecatedClassFqns.foreach(log.debug)
            if(verbose&&packagedeprecatedClassFqns.nonEmpty){
              log.warn("Lvl 2 class-hits (by package-info-deprecation): " + packagedeprecatedClassFqns.size)
            }
          case None =>
        }
      })
    }

    val methodsDeprecatedThroughClass = currentMethodsWithAccessFilter
      .filter(!_.isDeprecated)
      .filter(m => deprecatedClasses.contains(m.classFile.fqn))

    val methodsDeprecatedTCProt = methodsDeprecatedThroughClass
      .filter(_.isProtected)
      .map(m => s"${m.classFile.fqn}:${m.name}").toSet

    val methodsDeprecatedTCPub = methodsDeprecatedThroughClass
      .filter(_.isPublic)
      .map(m => s"${m.classFile.fqn}:${m.name}").toSet


    log.debug(s"Lvl 2+3 method-hits (by class-deprecation): " +
      s"${methodsDeprecatedThroughClass.size} = 📢 ${methodsDeprecatedTCPub.size} , 🛡️ ${methodsDeprecatedTCProt.size}")

    (deprecatedClasses,
      methodsDeprecatedTCPub.union(deprecatedPubMethods),
      methodsDeprecatedTCProt.union(deprecatedProtMethods))
  }

  def checkMethodSimilarity(method1: Method, method2: Method): Boolean = {
    // if there are *no* dissimilarities: true
    !(method1.accessFlags != method2.accessFlags ||
      method1.name != method2.name ||
      method1.descriptor != method2.descriptor)
  }

  override def getLibraryResults: Iterable[LibraryResultDB] = {
    val g = currentJarInfoDB.groupid
    val a = currentJarInfoDB.artifactname
    if(verbose){
      log.info(s"All unique Cdeprecation tags in $g:$a: ${allCDepr.size}")
      log.info(s"All unique Mdeprecation tags in $g:$a: ${allPubMDepr.size}  /  ${allProtMDepr.size}")
      log.info(s"All wrongly removed classes in $g:$a: $wronglyRemovedClasses")
      log.info(s"All wrongly removed methods in $g:$a: $wronglyRemovedMethods")
    }
    Seq(
      LibraryResultDB("allCDeprecations", g, a, allCDepr.size),
      LibraryResultDB("allMPubDeprecations", g, a, allPubMDepr.size),
      LibraryResultDB("allMProtDeprecations", g, a, allProtMDepr.size),
      LibraryResultDB("allWronglyRemC", g, a, wronglyRemovedClasses),
      LibraryResultDB("allWronglyRemM", g, a, wronglyRemovedMethods)
    )
  }

  /**
   * The name for this analysis implementation.
   */
  override def analysisName: String = "ClassDeprecation"
}