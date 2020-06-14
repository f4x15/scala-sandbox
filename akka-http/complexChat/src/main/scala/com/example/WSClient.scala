package com.example

import akka.{Done, NotUsed}
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}

import scala.concurrent.Future
import scala.util.Random

class WSClient(url: String, name: String, actorSystem: ActorSystem) {

  def spam(message: String, numberOfTimes: Int = 1000): Unit = {

    val talkActor = actorSystem.actorOf(Props(new Actor {

      implicit val system = ActorSystem()
      implicit val materializer = ActorMaterializer()
      import system.dispatcher
      import scala.concurrent.duration._

      var counter: Int = 0

      def receive: Receive = {
        case message: String =>
          counter = counter + 1

          // send some one on server
          // send(s"[$name] message #$counter")

          // sending message
          // val source = Source.single(TextMessage(s"[$name] message #$counter"))
          val source: Source[Message, ActorRef] =
            Source.actorRef[TextMessage.Strict](bufferSize = 10, OverflowStrategy.fail)
          val sink: Sink[Message, NotUsed] =
            Flow[Message]
              .map(message => println(s"Received text message: [$message]"))
              .to(Sink.ignore)
          val flow = Http().webSocketClientFlow(WebSocketRequest(url))

          val ((ws, upgradeResponse), closed) =
            source
              .viaMat(flow)(Keep.both)
              .toMat(sink)(Keep.both)
              .run()

          /*
          // sending Stream
          val rg = (source.toMat(flow)(Keep.right))
          val response = rg.run()
           */

          val connected = upgradeResponse.flatMap { upgrade =>
            if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
              Future.successful(Done)
            } else {
              throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
            }
          }

          ws ! TextMessage.Strict("Hello World")
          ws ! TextMessage.Strict("Hi")
          ws ! TextMessage.Strict("Yay!")

          /////////////

          if (counter < numberOfTimes)
            actorSystem.scheduler.scheduleOnce(rand.seconds, self, message)
      }

      def rand: Int = 1 + Random.nextInt(9) // 1-10 seconds
    }))

    // send message to created actor
    talkActor ! message

  }
}

object WSClient {
  def apply(url: String, name: String)(implicit actorSystem: ActorSystem): WSClient = {
    new WSClient(url, name, actorSystem)
  }
}