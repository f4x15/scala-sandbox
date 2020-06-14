package com.example

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.testkit.TestProbe
import org.scalatest.WordSpecLike

class DeviceSpec extends ScalaTestWithActorTestKit with WordSpecLike {
 // val system = ActorSystem

  "reply with empty reading if no temperature is known" in {
    val probe = TestProbe()
    val deviceActor = system.actorOf(Device.props("group", "device"))

    deviceActor.tell(Device.ReadTemperature(requestId = 42), probe.ref)
    val response = probe.expectMsgType[Device.RespondTemperature]
    response.requestId should ===(42)
    response.value should ===(None)
  }

}
