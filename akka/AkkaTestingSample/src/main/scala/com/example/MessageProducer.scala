package com.example

import akka.actor.typed.{ActorRef, Scheduler}
import akka.util.Timeout
import akka.actor.typed.scaladsl.AskPattern._

import scala.concurrent.Future
import scala.util.Try

// Classes for mock-test sample
case class Message(i: Int, replyTo: ActorRef[Try[Int]])

class Producer(publisher: ActorRef[Message])(implicit scheduler: Scheduler) {

  def produce(messages: Int)(implicit timeout: Timeout): Unit = {
    (0 until messages).foreach(publish)
  }

  private def publish(i: Int)(implicit timeout: Timeout): Future[Try[Int]] = {
    publisher.ask(ref => Message(i, ref))
  }

}
