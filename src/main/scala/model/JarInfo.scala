package model

import just.semver.SemVer
import util.SemVerUtils

import java.net.URL

/** Represents a row of the data table */
class JarInfo(row: (Int, String, String, Option[String], String, String, Int)) extends Ordered[JarInfo] {
  val id: Int = row._1
  val groupid: String = row._2
  val artifactname: String = row._3
  val classifier: Option[String] = row._4

  val SVU = new SemVerUtils()
  val version: SemVer = SVU.parseSemVerString(row._5)

  val url: URL = new URL(s"https://repo1.maven.org/maven2/${row._6}")
  val versionscheme: Int = row._7

  val jarname: String = s"$groupid:$artifactname-${row._5}"

  override def compare(that: JarInfo): Int = this.version.compare(that.version)
}

object JarInfo {
  def initial: JarInfo = new JarInfo(0, "", "", None, "0.0.0", "", 0)
}

