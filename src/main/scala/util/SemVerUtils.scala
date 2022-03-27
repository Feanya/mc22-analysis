package util

import just.semver.SemVer
import model._
import org.slf4j.{Logger, LoggerFactory}

class SemVerUtils() {
  val dummySemVer: SemVer = SemVer(SemVer.Major(9999), SemVer.Minor(9999), SemVer.Patch(9999), None, None)
  val log: Logger = LoggerFactory.getLogger("SemVerUtils")

  /**
   * Compare two version numbers and find the highest part of the version number that changed,
   * thus finding out if v1 -> v2 is considered a major, minor, patch, … upgrade
   * @param v1 first version
   * @param v2 second version
   * @return result
   */
  def calculateVersionjump(v1: SemVer, v2: SemVer): Versionjump = {
    if (v1.major.major == 0) MajorZero()
    if (v1.major.major == v2.major.major) {
      if (v1.minor.minor == v2.minor.minor) {
        if (v1.patch.patch == v2.patch.patch) Other()
        else if (v1.patch.patch < v2.patch.patch) Patch() else Other()
      }
      else if (v1.minor.minor < v2.minor.minor) Minor() else Other()
    }
    else if (v1.major.major < v2.major.major) Major() else Other()
  }


  /**
   * Safe version-string-parsing, fallback to dummy version.
   * @param version
   * @return SemVer object
   */
  def parseSemVerString(version: String): SemVer = {
    SemVer.parse(version) match {
      case Right(v) => v
      case _ => SemVer.parse(version + ".0") match {
        case Right(v) => log.debug(s"Filled up version with .0 to ${v}"); v
        case _ => log.error(s"Couldn't parse $version"); dummySemVer
      }
    }
  }
}
