package com.example.chat

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, Sink, Source, _}
import akka.stream.{FlowShape, OverflowStrategy}

/**
 * Chat room will be created for separate room
 *
 * @param roomId      - chat room ID
 * @param actorSystem - endpoint for every connected user
 */
class ChatRoom(roomId: Int, actorSystem: ActorSystem) {
  // the concrete actor for calculate room actions
  private[this] val chatRoomActor = actorSystem.actorOf(Props(classOf[ChatRoomActor], roomId))

  // returned Flow using for websocket connection processor
  def websocketFlow(user: String): Flow[Message, Message, _] =
    Flow.fromGraph(
      GraphDSL.create(Source.actorRef[ChatMessage](bufferSize = 5, OverflowStrategy.fail)) {
        implicit builder =>
          chatSource => // as parameter

            import GraphDSL.Implicits._

            //flow used as input, it takes Messages
            val fromWebSocket = builder.add(
              Flow[Message].collect {
                case TextMessage.Strict(txt) => IncomingMessage(user, txt)
              }
            )

            //flow used as output, it returns Messages
            val backToWebsocket = builder.add(
              Flow[ChatMessage].map {
                case ChatMessage(author, text) => TextMessage(s"[$author]: $text")
              }
            )


            // send messages to the actor, if send also UserLeft(user) before stream completes.
            val chatActorSink = Sink.actorRef[ChatEvent](chatRoomActor, UserLeft(user))

            // merges both pipes: input and generated messages
            val merge = builder.add(Merge[ChatEvent](2))

            // Materialized value of Actor who sit in chatroom
            val actorAsSource = builder.materializedValue.map(actor => UserJoined(user, actor))

            // Message from websocket is converted into IncommingMessage and should be send to each in room
            fromWebSocket ~> merge.in(0)

            // If Source actor is just created should be send as UserJoined and registered as particiant in room
            actorAsSource ~> merge.in(1)

            // Merges both pipes above and forward messages to chatroom Represented by ChatRoomActor
            merge ~> chatActorSink

            // Actor already sit in chatRoom so each message from room is used as source and pushed back into websocket
            chatSource ~> backToWebsocket

            // expose ports
            FlowShape(fromWebSocket.in, backToWebsocket.out)
      }
    )

  def sendMessage(message: ChatMessage): Unit = chatRoomActor ! message
}

object ChatRoom {
  def apply(roomId: Int)(implicit actorSystem: ActorSystem) = new ChatRoom(roomId, actorSystem)
}