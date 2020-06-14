package com.example.chat

import akka.actor.{Actor, ActorRef}
import scala.collection.mutable.Map
import com.example.chat

class ChatRoomActor(roomId: Int) extends Actor {

  // ActorRef here is it WebSocket-endpoint for connected client
  //  Create new actor for new chat-participants: `name`, `ActorRef`
  var participants: Map[String, ActorRef] = Map.empty[String, ActorRef]

  override def receive: Receive = {
    case UserJoined(name, actorRef) =>
      participants += name -> actorRef
      broadcast(SystemMessage(s"User $name joined channel..."))
      println(s"User $name joined channel[$roomId]")

    case UserLeft(name) =>
      println(s"User $name left channel[$roomId]")
      broadcast(SystemMessage(s"User $name left channel[$roomId]"))
      participants -= name

    case msg: IncomingMessage =>
      broadcast(msg)
  }

  def broadcast(message: ChatMessage): Unit = participants.values.foreach(_ ! message)
}