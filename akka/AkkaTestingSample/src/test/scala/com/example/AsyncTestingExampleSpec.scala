package com.example

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed._
import akka.actor.typed.scaladsl.Behaviors
import org.scalatest.{BeforeAndAfterAll, Matchers}
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration._
import scala.util.Success

/**
 * ActorTestKit sample.
 *  async classic tests
 * [based on](https://doc.akka.io/docs/akka/current/typed/testing-async.html)
 */
class AsyncTestingExampleSpec extends AnyWordSpec
    with BeforeAndAfterAll
    with Matchers {

  // create test kit based on ActorSystem with spawn, call, shutdown functions
  val testKit = ActorTestKit()

  // we need shutdown ActorSystem by hands
  override def afterAll(): Unit = testKit.shutdownTestKit()

  // some test-case: create obj, probe and verify
  val pinger = testKit.spawn(Echo(), "ping")
  val probe = testKit.createTestProbe[Echo.Pong]()
  pinger ! Echo.Ping("hello", probe.ref)
  probe.expectMessage(Echo.Pong("hello"))

  // --------------- Stop-timeouts
  val pinger1 = testKit.spawn(Echo(), "pinger")
  pinger1 ! Echo.Ping("hello", probe.ref)
  probe.expectMessage(Echo.Pong("hello"))
  testKit.stop(pinger1) // Uses default timeout

  // Immediately creating an actor with the same name
  val pinger2 = testKit.spawn(Echo(), "pinger")
  pinger2 ! Echo.Ping("hello", probe.ref)
  probe.expectMessage(Echo.Pong("hello"))
  testKit.stop(pinger2, 10.seconds) // Custom timeout
  // ---------------------------

  // --------- mock testing
  /*
  import testKit._

  // simulate the happy path
  val mockedBehavior = Behaviors.receiveMessage[Message] { msg =>
    msg.replyTo ! Success(msg.i)
    Behaviors.same
  }
  val probe2 = testKit.createTestProbe[Message]()
  val mockedPublisher = testKit.spawn(Behaviors.monitor(probe2.ref, mockedBehavior))

  // test our component
  val producer = new Producer(mockedPublisher)
  val messages = 3
  producer.produce(messages)

  // verify expected behavior
  for (i <- 0 until messages) {
    val msg = probe2.expectMessageType[Message]
    msg.i shouldBe i
  }
  */
  //----------------------------

}