package com.example

import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import com.example.services.EchoService
import org.scalatest.{FlatSpecLike, FunSuite, Matchers}

class EchoServiceTest extends FlatSpecLike with Matchers with ScalatestRouteTest {
  "Echo Websocket" should "response by websocket connection" in {
    val wsClient = WSProbe()

    WS("/ws-echo", wsClient.flow) ~> EchoService.route ~> check {

      this should be an 'isWebsocketUpgrade

      wsClient.sendMessage("Mario")
      wsClient.expectMessage("ECHO: Mario")

      wsClient.sendCompletion()
      wsClient.expectCompletion()
    }
  }
}
