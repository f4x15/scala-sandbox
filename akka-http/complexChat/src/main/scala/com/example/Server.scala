package com.example

// sample based on:
//  https://scalac.io/websockets-server-with-akka-http/
//  https://github.com/ticofab/akka-http-websocket-example
//  https://github.com/ScalaConsultants/wsug-akka-websockets/tree/master/src/main/scala/io/scalac/wsakka

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.example.services.{ChatService, EchoService, MainService}

import scala.io.StdIn

object Server extends App {
  implicit val actorSystem = ActorSystem("akka-system")
  implicit val flowMaterializer = ActorMaterializer()

  val config = actorSystem.settings.config
  val interface = config.getString("app.interface")
  val port = config.getInt("app.port")

  val route = MainService.route ~ // combine the routes
    EchoService.route ~
    ChatService.route

  val binding = Http().bindAndHandle(route, interface, port)

  println(s"Server is now online at http://$interface:$port\nPress RETURN to stop...")
  StdIn.readLine()

  import actorSystem.dispatcher

  binding.flatMap(_.
    unbind()). // trigger unbinding from the port
    onComplete(_ => actorSystem.terminate()) // and shutdown when done

  println("Server is down...")
}