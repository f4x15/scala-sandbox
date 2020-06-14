package com.example.services

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.example.chat.ChatRooms

object ChatService {

  def route(implicit actorSystem: ActorSystem, materializer: Materializer): Route =

  // path on concrete chat room `XXX` with `YYY` name: `/ws-chat/XXX?name=YYYY`
    pathPrefix("ws-chat" / IntNumber) { chatId => // IntNumber directive to chatId
      parameter('name) { userName => // parameter match `name` parameter to userName
        // find or create concrete chat-room, than get concrete Actor/Flow for this room
        handleWebSocketMessages(ChatRooms.findOrCreate(chatId).websocketFlow(userName))
      }
    }
}
