package com.example.streaming
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.UnsupportedRequestContentTypeRejection
import com.example.TestTemplate
import com.example.streaming.JsonRequestStreaming

class JsonRequestStreamingSpec extends TestTemplate{
  // tested route
  lazy val route = JsonRequestStreaming.route

  "JsonRequestStreamingSpec" should {
    "spray-json-request-streaming" in {
      // uploading an array or newline separated values works out of the box
      val data = HttpEntity(
        ContentTypes.`application/json`,
        """
          |{"id":"temp","value":32}
          |{"id":"temp","value":31}
          |
        """.stripMargin)

      Post("/metrics", entity = data) ~> route ~> check {
        status should ===(StatusCodes.OK)
        responseAs[String] should ===("""{"msg":"Total metrics received: 2"}""")
      }

    }

    "reject understand type in" in {
      // the FramingWithContentType will reject any content type that it does not understand:
      val xmlData = HttpEntity(
        ContentTypes.`text/xml(UTF-8)`,
        """|<data id="temp" value="32"/>
           |<data id="temp" value="31"/>""".stripMargin)

      Post("/metrics", entity = xmlData) ~> route ~> check {
        handled should ===(false)
        rejection should ===(
          UnsupportedRequestContentTypeRejection(
            Set(ContentTypes.`application/json`),
            Some(ContentTypes.`text/xml(UTF-8)`)))
      }
    }
  }
}
