package com.example

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

/**
 *  Core of the chat-room
 *  The chat room Actor will disseminate all posted messages to all currently connected client Actors.
 */
object ChatRoom {

  //<editor-fold desc="Request session by client API">

  sealed trait RoomCommand
  /**
   * Request the session by client
   * @param screenName - name of the client
   * @param replyTo - the client
   */
  final case class GetSession(screenName: String, replyTo: ActorRef[SessionEvent]) extends RoomCommand

  /**
   * Message posted event wrapper (from `PostMessage`) with `screenName`
   * @param screenName - nickname of the client
   * @param message - posted messages
   */
  private final case class PublishSessionMessage(screenName: String, message: String) extends RoomCommand
  //</editor-fold>

  //<editor-fold desc="Session events">

  sealed trait SessionEvent
  /**
   * Session establish flag
   * @param handle - actor tof next session steps
   */
  final case class SessionGranted(handle: ActorRef[PostMessage]) extends SessionEvent
  final case class SessionDenied(reason: String) extends SessionEvent
  //</editor-fold>

  //<editor-fold desc="Message posted">
  /**
   * Notify all subscribers in the chat that messages posted
   * @param screenName - name of the poster
   * @param message - message
   */
  final case class MessagePosted(screenName: String, message: String) extends SessionEvent
  //</editor-fold>

  //<editor-fold desc="Session commands">

  trait SessionCommand
  /**
   * Message posting event
   * @param message
   */
  final case class PostMessage(message: String) extends SessionCommand
  private final case class NotifyClient(message: MessagePosted) extends SessionCommand
  //</editor-fold>

  def apply(): Behavior[RoomCommand] =
    chatRoom(List.empty)

  private def chatRoom(sessions: List[ActorRef[SessionCommand]]): Behavior[RoomCommand] =
    Behaviors.receive { (context, message) =>
      message match {
        case GetSession(screenName, client) =>
          // create a child actor for further interaction with the client
          val ses = context.spawn(
            session(context.self, screenName, client),    // create session actor
            name = URLEncoder.encode(screenName, StandardCharsets.UTF_8.name))
          client ! SessionGranted(ses)
          chatRoom(ses :: sessions) // add ses to sessions list: 'what :: where'

        // notify all participant about new messages in room
        case PublishSessionMessage(screenName, message) =>
          val notification = NotifyClient(MessagePosted(screenName, message))
          sessions.foreach(_ ! notification)
          Behaviors.same
      }
    }

  // Internal `session` actor
  private def session(
                       room: ActorRef[PublishSessionMessage],
                       screenName: String,
                       client: ActorRef[SessionEvent]): Behavior[SessionCommand] =
    Behaviors.receiveMessage {
      case PostMessage(message) =>
        // from client, publish to others via the room
        room ! PublishSessionMessage(screenName, message)   // wrapper for message
        Behaviors.same
      case NotifyClient(message) =>
        // published from the room
        client ! message
        Behaviors.same
    }
}