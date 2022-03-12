package util

import org.slf4j.{Logger, LoggerFactory}

import java.net.URL
class PairGenerator (){

  protected val log: Logger = LoggerFactory.getLogger(this.getClass)

  def generateURLPairs(urls: Seq[URL]): Seq[(URL, URL)] = {
    urls.zip(urls)
    // todo: generate senseful pairs
  }

  def generateProjectPairs(): Unit = {}
}

