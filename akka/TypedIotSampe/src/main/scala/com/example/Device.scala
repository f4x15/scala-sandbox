package com.example

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors, LoggerOps}
import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}

object Device {
  //The apply method in the companion object defines how to construct the Behavior
  // for the Device actor.
  def apply(groupId: String, deviceId: String): Behavior[Command] =
    Behaviors.setup(context => new Device(context, groupId, deviceId))

  // device interface
  sealed trait Command
  // requestId - for control-flow
  final case class ReadTemperature(requestId: Long, replyTo: ActorRef[RespondTemperature])
    extends Command   // request temperature
  final case class RespondTemperature(requestId: Long, deviceId: String, value: Option[Double])   // send temperature

  // We have seen that Akka does not guarantee delivery of these messages and leaves it
  //  to the application to provide success notifications.
  final case class RecordTemperature(requestId: Long, value: Double, replyTo: ActorRef[TemperatureRecorded])
    extends Command   // record temperature command
  final case class TemperatureRecorded(requestId: Long) // ACK-confirmation

  case object Passivate extends Command   // stop the Device
}

class Device(context: ActorContext[Device.Command], groupId: String, deviceId: String)
  extends AbstractBehavior[Device.Command](context) {
  import Device._

  var lastTemperatureReading: Option[Double] = None

  context.log.info2("Device actor {}-{} started", groupId, deviceId)

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case RecordTemperature(id, value, replyTo) =>
        context.log.info2("Recorded temperature reading {} with {}", value, id)
        lastTemperatureReading = Some(value)
        replyTo ! TemperatureRecorded(id)
        this

      case ReadTemperature(id, replyTo) =>
        replyTo ! RespondTemperature(id, deviceId, lastTemperatureReading)
        this

      case Passivate =>
        Behaviors.stopped
    }
  }

  override def onSignal: PartialFunction[Signal, Behavior[Command]] = {
    case PostStop =>
      context.log.info2("Device actor {}-{} stopped", groupId, deviceId)
      this
  }
}