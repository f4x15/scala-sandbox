package com.example

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import com.example.GreeterMain.SayHello

/**
 * Receives commands to Greet someone and responds with a Greeted to confirm
 *  the greeting has taken place
 */
object Greeter {
  // Messages Actor-interface
  final case class Greet(whom: String, replyTo: ActorRef[Greeted]) // command to Greeter
  final case class Greeted(whom: String, from: ActorRef[Greet]) // reply from Greeter

  // Behavior and interface of Actor
  // Actor initials behaviour hold in `apply()` method
  // Behaviors.receive is a behavior factory, it can return new behavior
  //
  // Processing the next messages than result a new behaviour that the potencially
  //  different from current. State is updated bt returning new behaviour that holds
  //  a new immutable state. Behaviour can be the same: `Behaviors.same`
  // Use Greeter() as method that return `Behavior[Greet]`
  def apply(): Behavior[Greet] = Behaviors.receive { (context, message) =>
    context.log.info("Hello {}!", message.whom)
    message.replyTo ! Greeted(message.whom, context.self)   // send message to repleyer
    Behaviors.same
  }
}

/**
 * Receives the reply from the Greeter and sends a number of additional greeting messages
 * and collect the replies until a given max number of messages have been reached.
 */
object GreeterBot {
  // create method `GreeterBot(3)`
  def apply(max: Int): Behavior[Greeter.Greeted] = {
    bot(0, max)
  }

  // Calling Actors n-times
  private def bot(greetingCounter: Int, max: Int): Behavior[Greeter.Greeted] =
    Behaviors.receive { (context, message) =>
      val n = greetingCounter + 1

      context.log.info("Greeting {} for {}", n, message.whom)
      if (n == max) {
        Behaviors.stopped
      } else {
        message.from ! Greeter.Greet(message.whom, context.self)
        bot(n, max)
      }
    }
}

/**
 * `The guardian` actor that bootstraps everything
 */
object GreeterMain {
  final case class SayHello(name: String)

  def apply(): Behavior[SayHello] =
    Behaviors.setup { context =>

      // `Creeter()` Call Greeter `apply()` method
      // Create new ActorRef, Greeter type with given name
      val greeter = context.spawn(Greeter(), "greeter")

      Behaviors.receiveMessage { message =>

        val replyTo = context.spawn(GreeterBot(max = 3), message.name)
        greeter ! Greeter.Greet(message.name, replyTo)
        Behaviors.same
      }
    }
}

object AkkaQuickstart extends App {

  // Actor system has name and guardian actor
  // here `guardian actor` is GreeterMain
  val greeterMain: ActorSystem[GreeterMain.SayHello] = ActorSystem(GreeterMain(), "AkkaQuickStart")
  greeterMain ! SayHello("Charles")
}