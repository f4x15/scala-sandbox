package com.example.websocket

// WebSocket example in low-level-api
//  based on: https://doc.akka.io/docs/akka-http/current/server-side/websocket-support.html
//  change protocol to: https://stackoverflow.com/questions/26401893/who-is-sending-http-websocket-upgrade-requests

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage, UpgradeToWebSocket}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.model.HttpMethods._

import scala.io.StdIn

object WebSocketLowLevelSample extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  // The Greeter WebSocket Service expects a "name" per message and
  // returns a greeting message for that name
  val greeterWebSocketService =
  Flow[Message]
    .mapConcat {
      // we match but don't actually consume the text message here,
      // rather we simply stream it back as the tail of the response
      // this means we might start sending the response even before the
      // end of the incoming message has been received
      case tm: TextMessage => TextMessage(Source.single("Hello ") ++ tm.textStream) :: Nil
      case bm: BinaryMessage =>
        // ignore binary messages but drain content to avoid the stream being clogged
        bm.dataStream.runWith(Sink.ignore)
        Nil
    }

  // httpRequest => into response
  val requestHandler: HttpRequest => HttpResponse = {
    case req@HttpRequest(GET, Uri.Path("/greeter"), _, _, _) =>
      // greeter are expected to be WebSocket requests
      req.header[UpgradeToWebSocket] match {    // if exist request for WebSocket header
        case Some(upgrade) => upgrade.handleMessages(greeterWebSocketService)
        case None => HttpResponse(400, entity = "Not a valid websocket request!")
      }
    case r: HttpRequest =>
      r.discardEntityBytes() // important to drain incoming HTTP Entity stream
      HttpResponse(404, entity = "Unknown resource!")
  }

  //<editor-fold desc="Start server routine">

  val bindingFuture =
    Http().bindAndHandleSync(requestHandler, interface = "localhost", port = 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()

  import system.dispatcher // for the future transformations
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

  //</editor-fold>
}