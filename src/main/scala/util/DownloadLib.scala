package util

import opal.OPALProjectHelper
import org.opalj.br.analyses.Project
import org.slf4j.{Logger, LoggerFactory}

import java.net.URL
import scala.util.{Failure, Success}


class DownloadLib() {

  protected val log: Logger = LoggerFactory.getLogger(this.getClass)

  def downloadAndLoadOne(url: URL): Option[Project[URL]] = {
    val downloader = new HttpDownloader
    downloader.downloadFromUri(url.toString) match {
      case Success(jarStream) =>
        println(s"Initializing OPAL project for input $url ...")
        val projectClasses = OPALProjectHelper.readClassesFromJarStream(jarStream, url)

        // build and return the project
        val project = OPALProjectHelper.buildOPALProject(
          projectClasses.get, List.empty, asLibrary = false, excludeJRE = true)
        log.debug(project.statistics.mkString("\n"))
        Some(project)

      case Failure(ex@HttpException(code)) if code.intValue() == 404 =>
        log.warn(s"${ex.getMessage} \nNo JAR file could be located at $url")
        None
      case Failure(ex) =>
        log.error(s"Failed to download JAR file at $url")
        None
      case _ =>
        None
    }
  }

}

