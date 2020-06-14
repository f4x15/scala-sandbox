package com.example.chat

import akka.actor.ActorSystem

/**
 * Chat rooms holder
 */
object ChatRooms {
  // chat rooms holder
  var chatRooms: Map[Int, ChatRoom] = Map.empty[Int, ChatRoom]

  def findOrCreate(number: Int)(implicit actorSystem: ActorSystem): ChatRoom =
    chatRooms.getOrElse(number, createNewChatRoom(number))

  private def createNewChatRoom(number: Int)(implicit actorSystem: ActorSystem): ChatRoom = {
    val chatroom = ChatRoom(number)
    chatRooms += number -> chatroom // `->` associated `key` -> `value` with value in dictionary

    chatroom
  }
}
