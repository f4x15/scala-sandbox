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

import scala.io.StdIn

/**
 * Example of custom response render
 *
 * Configure the EntityStreamingSupport to render the elements as:
 * {"example":42}
 * {"example":43}
 * ...
 * {"example":1000}
 * It is not correct Json, but often uses because of simple for parsing on a client
 *
 * based on: https://doc.akka.io/docs/akka-http/current/routing-dsl/source-streaming-support.html#customising-response-rendering-mode
 */
object CustomResponseTweetRender {
  import MyTweetJsonProtocol._
  import Tweet._

  //<editor-fold desc="Custom client render">

  val newline = ByteString("\n")

  implicit val jsonStreamingSupport = EntityStreamingSupport.json()
    .withFramingRenderer(Flow[ByteString].map(bs => bs ++ newline))
    .withParallelMarshalling(parallelism = 8, unordered = true)    // paralleling marshalling in unorder
  //</editor-fold>

  val route =
    path("tweets") {
      val tweets: Source[Tweet, NotUsed] = getTweets
      complete(tweets)
    }
}
