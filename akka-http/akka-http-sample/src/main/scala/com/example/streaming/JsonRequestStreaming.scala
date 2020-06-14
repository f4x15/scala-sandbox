package com.example.streaming

import akka.NotUsed
import akka.http.scaladsl.common.{EntityStreamingSupport, JsonEntityStreamingSupport}
import akka.stream.scaladsl.Source
import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.util.ByteString
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.concurrent.Future

/**
 * Consuming JSON Streaming uploads
 *
 * Sometimes a client sends a streaming request. For example, an embedded device initiated
 * a connection with the server and is feeding it with one line of  stream measurement data.
 *
 * In this example, we want to consume this data in a streaming fashion from the request entity and also apply back pressure to the underlying TCP connection should the server be unable to cope with the rate of incoming data.
 *
 * based on: https://doc.akka.io/docs/akka-http/current/routing-dsl/source-streaming-support.html#customising-response-rendering-mode
 */
case class Measurement(id: String, value: Int)

object MyMeasurementJsonProtocol
  extends akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
    with spray.json.DefaultJsonProtocol {

  implicit val measurementFormat = jsonFormat2(Measurement.apply)
}

object JsonRequestStreaming {
  //<editor-fold desc="prepare actor system">

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  //</editor-fold>

  // enable Json Streaming
  implicit val jsonStreamingSupport = EntityStreamingSupport.json()

  import MyMeasurementJsonProtocol._

  // prepare your persisting logic here
  val persistMetrics = Flow[Measurement]

  val route =
    path("metrics") {
      // [3] extract Source[Measurement, _]
      entity(asSourceOf[Measurement]) { measurements =>
        // alternative syntax:
        // entity(as[Source[Measurement, NotUsed]]) { measurements =>
        val measurementsSubmitted: Future[Int] =
          measurements
            .via(persistMetrics)
            .runFold(0) { (cnt, _) => cnt + 1 }

        complete {
          measurementsSubmitted.map(n => Map("msg" -> s"""Total metrics received: $n"""))
        }
      }
    }
}
