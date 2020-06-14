package com.example

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.example.WebServerMarshalling.{Item, Order}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import akka.actor.typed.scaladsl.adapter._
import spray.json.DefaultJsonProtocol
import spray.json.DefaultJsonProtocol.{jsonFormat1, jsonFormat3}


class WebServerMarshallingSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  // As TestKit can't use typed Actors, we need transform it to classic
  // TODO: try make typed system
  lazy val testKit = ActorTestKit()
  implicit def typedSystem = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem = testKit.system.toClassic

  //val userRegistry = testKit.spawn(UserRegistry())
  lazy val route =  WebServerMarshalling.route //  new UserRoutes(userRegistry).userRoutes

  // use the json formats to marshal and unmarshall objects in the test
  import DefaultJsonProtocol._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  implicit val itemJsonFormat = jsonFormat2(Item)
  implicit val orderJsonFormat = jsonFormat1(Order)

  "WebServer" should {
    "Create new order (Post /create-order)" in {
      val order = Order(List(Item("Ivan", 12)))

      // to add the entity we’ve used the `Marshal(object).to[TargetType]` syntax
      val orderEntity = Marshal(order).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      // using the RequestBuilding DSL moq:
      val request = Post("/create-order").withEntity(orderEntity)

      request ~> route ~> check {
        status should === (StatusCodes.Created)

        // we expect the response to be text:
        contentType should === (ContentTypes.`text/plain(UTF-8)`)

        // and no entries should be in the list:
        entityAs[String] should ===("""order created""")
      }

    }
  }
}
