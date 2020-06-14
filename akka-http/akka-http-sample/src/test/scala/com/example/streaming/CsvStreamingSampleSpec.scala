package com.example.streaming

import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.model.{MediaRange, MediaTypes}
import com.example.TestTemplate

class CsvStreamingSampleSpec extends TestTemplate{
  // tested route
  lazy val route = CsvStreamingSample.route

  "CsvStreamingSampleSpec" should {
    "CSV stream sample" in {
      val AcceptCsv = Accept(MediaRange(MediaTypes.`text/csv`))

      Get("/tweets").withHeaders(AcceptCsv) ~> route ~> check {
        responseAs[String] shouldEqual
          "1,#Akka rocks!" + "\n" +
            "2,Streaming is so hot right now!" + "\n" +
            "3,You cannot enter the same river twice." + "\n"
      }
    }}


}
