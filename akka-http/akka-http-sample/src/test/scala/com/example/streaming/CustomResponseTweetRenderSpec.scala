package com.example.streaming

import akka.http.scaladsl.model.{ContentTypes, MediaRange, MediaTypes}
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.server.UnacceptedResponseContentTypeRejection
import com.example.TestTemplate

class CustomResponseTweetRenderSpec extends TestTemplate {
  // tested route
  lazy val route = CustomResponseTweetRender.route

  "CustomResponseTweetRender" should {
    "Custom JSON correct rendered ({item1}...{itemn})" in {
      val AcceptJson = Accept(MediaRange(MediaTypes.`application/json`))

      Get("/tweets").withHeaders(AcceptJson) ~> route ~> check {
        responseAs[String] shouldEqual
          """{"txt":"#Akka rocks!","uid":1}""" + "\n" +
            """{"txt":"Streaming is so hot right now!","uid":2}""" + "\n" +
            """{"txt":"You cannot enter the same river twice.","uid":3}""" + "\n"
      }
    }
  }
}