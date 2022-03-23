/**
 * @author: The Delphi Team 2018
 */

package util

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCode, StatusCodes}
import akka.stream.ActorMaterializer
import akka.util.ByteString

import java.io.{ByteArrayInputStream, InputStream}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.util.{Failure, Try}

case class HttpException(code: StatusCode) extends Throwable {
  override def getMessage: String = s"Http Request failed with status code ${code.intValue()}"
}

class HttpDownloader() {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

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
