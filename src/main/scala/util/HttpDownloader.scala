/**
 * @author: The Delphi Team 2018
 */

package util

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCode, StatusCodes}
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.typesafe.config.{Config, ConfigFactory}

import java.io.{ByteArrayInputStream, InputStream}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.{Failure, Try}

case class HttpException(code: StatusCode) extends Throwable {
  override def getMessage: String = s"Http Request failed with status code ${code.intValue()}"
}

// todo rework/update to newer version of Akka
class HttpDownloader() {
  println("⬇ ️Downloader startup")
  val config: Config = ConfigFactory.parseString("akka.loglevel = WARNING, akka.log-dead-letters = off, akka.logger-startup-timeout = 60s")
  // todo: if this still doesn't work out, set higher paralellism-min (https://groups.google.com/g/akka-user/c/nng-bb2IZFA)
  implicit val system: ActorSystem = ActorSystem("httpdownloader", config)
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  //implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  def downloadFromUri(requestedUri: String): Try[InputStream] = {
    val responseFuture: Future[HttpResponse] =
      Http().singleRequest(HttpRequest(uri = requestedUri))

    Await.result(responseFuture, Duration.Inf) match {
      case HttpResponse(StatusCodes.OK, _, entity, _) =>
        Try(new ByteArrayInputStream(Await.result(entity.dataBytes.runFold(ByteString.empty)(_ ++ _).map(_.toArray), Duration.Inf)))
      case resp@HttpResponse(code, _, _, _) =>
        resp.discardEntityBytes()
        Failure(HttpException(code))
    }
  }

}
