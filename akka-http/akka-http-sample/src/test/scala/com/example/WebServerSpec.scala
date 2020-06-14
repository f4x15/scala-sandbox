package com.example

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }
import akka.actor.typed.scaladsl.adapter._

class WebServerSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  // As TestKit can't use typed Actors, we need transform it to classic
  lazy val testKit = ActorTestKit()
  implicit def typedSystem = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem = testKit.system.toClassic

  //val userRegistry = testKit.spawn(UserRegistry())
  lazy val route =  WebServer.route //  new UserRoutes(userRegistry).userRoutes

  "WebServer" should {
    "return hello on (Get /hello)" in {
      // note that there's no need for the host part in the uri:
      val request = HttpRequest(uri = "/hello")

      request ~> route ~> check {
        status should === (StatusCodes.OK)

        // we expect the response to be text:
        contentType should ===(ContentTypes.`text/html(UTF-8)`)

        // and no entries should be in the list:
        entityAs[String] should ===("""<h1>Say hello to akka-http</h1>""")
      }

    }
  }
}
