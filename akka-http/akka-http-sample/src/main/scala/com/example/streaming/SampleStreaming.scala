package com.example.streaming

// based on: https://doc.akka.io/docs/akka-http/current/introduction.html

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.util.ByteString
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpEntity, ContentTypes }
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.util.Random
import scala.io.StdIn

object SampleStreaming {

  /*
  One of the strengths of Akka HTTP is that streaming data is at its heart meaning that
  both request and response bodies can be streamed through the server achieving constant
  memory usage even for very large requests or responses. Streaming responses will
  be backpressured by the remote client so that the server will not push data faster
  than the client can handle, streaming requests means that the server decides how fast
  the remote client can push the data of the request body.

  Connecting to this service with a slow HTTP client would backpressure so that the
  next random number is produced on demand with constant memory usage on the server.
  :This can be seen using curl and limiting the rate curl --limit-rate 50b 127.0.0.1:8080/random
   */
  def main(args: Array[String]) {

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    // streams are re-usable so we can define it here
    // and use it for every request
    val numbers = Source.fromIterator(() =>
      Iterator.continually(Random.nextInt()))

    val route =
      path("random") {
        get {
          complete(
            HttpEntity(
              ContentTypes.`text/plain(UTF-8)`,
              // transform each number to a chunk of bytes
              numbers.map(n => ByteString(s"$n\n"))
            )
          )
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
