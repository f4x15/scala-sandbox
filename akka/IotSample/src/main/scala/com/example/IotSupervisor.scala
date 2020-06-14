package com.example

import akka.actor.{ Actor, ActorLogging, Props }

object IotSupervisor {
  // Typicly pattern
  // props is a confguration object using in creating Actor
  def props(): Props = Props(new IotSupervisor)
}

/**
 * Main system Actor
 */
class IotSupervisor extends Actor with ActorLogging {
  override def preStart(): Unit = log.info("IoT Application started")
  override def postStop(): Unit = log.info("IoT Application stopped")

  // No need to handle any messages
  override def receive = Actor.emptyBehavior
}