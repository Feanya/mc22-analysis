package util

import just.semver.SemVer
import just.semver.SemVer.render
import model._
import org.slf4j.{Logger, LoggerFactory}

class SemVerUtils() {
  val dummySemVer: SemVer = SemVer(SemVer.Major(9999), SemVer.Minor(9999), SemVer.Patch(9999), None, None)
  val log: Logger = LoggerFactory.getLogger("SemVerUtils")

  /**
   * Compare two version numbers and find the highest part of the version number that changed,
   * thus finding out if v1 -> v2 is considered a major, minor, patch, … upgrade
   *
   * Everything that does not "make sense" aka e.g. decreasing major version is labelled "Other".
   *
   * @param v1 first version
   * @param v2 second version
   * @return result
   */
  def calculateVersionjump(v1: SemVer, v2: SemVer): Versionjump = {
    // first fishing out those where the later has major version 0
    if (v2.major.major == 0){
      if (v1.minor.minor == v2.minor.minor) {
        if (v1.patch.patch == v2.patch.patch) Same()
        else if (v1.patch.patch < v2.patch.patch) MajorZeroPatch() else other(v1, v2)
      }
      else if (v1.minor.minor < v2.minor.minor) MajorZeroMinor() else other(v1, v2)
    }
    // at least the second version should be higher than 0 now
    else if (v1.major.major == v2.major.major) {
      if (v1.minor.minor == v2.minor.minor) {
        if (v1.patch.patch == v2.patch.patch) Same()
        else if (v1.patch.patch < v2.patch.patch) Patch() else other(v1, v2)
      }
      else if (v1.minor.minor < v2.minor.minor) Minor() else other(v1, v2)
    }
    else if (v1.major.major < v2.major.major) Major() else other(v1, v2)
  }

  private def other(v1: SemVer, v2: SemVer): Versionjump ={
    log.error(s"Weird things happening when comparing ${render(v1)} and ${render(v2)}")
    Other()
  }


  /**
   * Safe version-string-parsing, fallback to dummy version.
   *
   * @param version version as string
   * @return SemVer object
   */
  def parseSemVerString(version: String): SemVer = {
    SemVer.parse(version) match {
      case Right(v) => v
      case _ => SemVer.parse(version + ".0") match {
        case Right(v) => log.debug(s"Filled up version with .0 to $v"); v
        case _ => log.error(s"Couldn't parse $version"); dummySemVer
      }
    }
  }
}
