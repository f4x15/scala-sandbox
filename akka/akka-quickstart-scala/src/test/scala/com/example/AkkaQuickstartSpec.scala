package com.example
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.example.Greeter.Greet
import com.example.Greeter.Greeted
import org.scalatest.WordSpecLike

class AkkaQuickstartSpec extends ScalaTestWithActorTestKit with WordSpecLike {
  "A Greeter" must {
    "reply to greeted" in {
      val replyProbe = createTestProbe[Greeted]()
      val underTest = spawn(Greeter())
      underTest ! Greet("Santa", replyProbe.ref)
      replyProbe.expectMessage(Greeted("Santa", underTest.ref))
    }
  }
}

