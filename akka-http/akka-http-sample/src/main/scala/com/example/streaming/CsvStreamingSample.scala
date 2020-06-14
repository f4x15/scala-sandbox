package com.example.streaming

import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshalling.{Marshaller, Marshalling}
import akka.http.scaladsl.model.ContentTypes
import akka.util.ByteString

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

import com.example.streaming.Tweet._

object CsvStreamingSample {
  // [1] provide a marshaller to ByteString
  implicit val tweetAsCsv = Marshaller.strict[Tweet, ByteString] { t =>
    Marshalling.WithFixedContentType(ContentTypes.`text/csv(UTF-8)`, () => {
      val txt = t.txt.replaceAll(",", ".")
      val uid = t.uid
      ByteString(List(uid, txt).mkString(","))
    })
  }

  // [2] enable csv streaming:
  implicit val csvStreaming = EntityStreamingSupport.csv()

  val route =
    path("tweets") {
      val tweets: Source[Tweet, NotUsed] = getTweets
      complete(tweets)
    }
}
