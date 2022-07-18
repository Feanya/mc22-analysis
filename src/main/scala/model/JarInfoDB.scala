package model

import just.semver.SemVer


case class JarInfoDB(id: Int,
                     groupid: String,
                     artifactname: String,
                     classifier: Option[String],
                     version: SemVer,
                     path: String,
                     versionscheme: Int,
                     size: Double,
                     jarname: String) extends Ordered[JarInfoDB] {

  override def compare(that: JarInfoDB): Int = this.version.compare(that.version)
}

object JarInfoDB {
  def initial: JarInfoDB = new JarInfoDB(
    id = 0,
    groupid = "",
    artifactname = "", None,
    version = SemVer.withMajor(SemVer.major0),
    path = "",
    versionscheme = 0,
    size = 0,
    jarname = "")
}

