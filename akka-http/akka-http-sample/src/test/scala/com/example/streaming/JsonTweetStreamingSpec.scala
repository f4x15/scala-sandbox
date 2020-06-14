package com.example.streaming

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.server.UnacceptedResponseContentTypeRejection
import spray.json.DefaultJsonProtocol
import spray.json.DefaultJsonProtocol.{jsonFormat1, jsonFormat3}

class JsonTweetStreamingSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest {
  // As TestKit can't use typed Actors, we need transform it to classic
  // TODO: try make typed system
  lazy val testKit = ActorTestKit()
  implicit def typedSystem = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem = testKit.system.toClassic

  // tested route
  lazy val route =  JsonTweetStreaming.route

  "JsonTweetStreaming" should {
    "spray-json-response-streaming" in {
      // used by user agents to specify response media types that are acceptable.
      val AcceptJson = Accept(MediaRange(MediaTypes.`application/json`))
      val AcceptXml = Accept(MediaRange(MediaTypes.`text/xml`))

      Get("/tweets").withHeaders(AcceptJson) ~> route ~> check {
        responseAs[String] shouldEqual
          """[""" +
            """{"txt":"#Akka rocks!","uid":1},""" +
            """{"txt":"Streaming is so hot right now!","uid":2},""" +
            """{"txt":"You cannot enter the same river twice.","uid":3}""" +
            """]"""
      }

      // endpoint can only marshal Json, so it will *reject* requests for application/xml:
      Get("/tweets").withHeaders(AcceptXml) ~> route ~> check {
        handled should ===(false)
        rejection should ===(UnacceptedResponseContentTypeRejection(Set(ContentTypes.`application/json`)))
      }
    }
  }

}
