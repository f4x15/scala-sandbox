package com.example.websocket

// WebSockets high-level api

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage, UpgradeToWebSocket}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.model.HttpMethods._

import scala.concurrent.duration._
import akka.util.ByteString
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.example.websocket.WebSocketLowLevelSample.{requestHandler, system}

import scala.io.StdIn

object WebSocketHighLevelSample extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  def greeter: Flow[Message, Message, Any] =
    Flow[Message].mapConcat {
      case tm: TextMessage =>
        TextMessage(Source.single("Hello ") ++ tm.textStream ++ Source.single("!")) :: Nil
      case bm: BinaryMessage =>
        // ignore binary messages but drain content to avoid the stream being clogged
        bm.dataStream.runWith(Sink.ignore)
        Nil
    }
  val webSocketRoute =
    path("greeter") {
      handleWebSocketMessages(greeter)  // handle WebSocket and reject others
    }

  //<editor-fold desc="Start server routine">

  val bindingFuture =
    Http().bindAndHandle(webSocketRoute, interface = "localhost", port = 8080)
  // THINK: change it to Http().bindAndHandleSync ???

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()

  import system.dispatcher // for the future transformations
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

  //</editor-fold>
}
