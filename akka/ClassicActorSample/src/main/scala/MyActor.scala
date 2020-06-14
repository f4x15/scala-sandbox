import akka.actor.{Actor, ActorSystem, Props}
import akka.event.Logging

class MyActor extends Actor {
  val log = Logging(context.system, this)

  def receive = {
    case "test" => log.info("received test")
    case _      => log.info("received unknown message")
  }
}

object MyActor extends App {
  val system = ActorSystem("MyActorSystem")
  // default Actor constructor
  val myActor = system.actorOf(Props[MyActor], name = "MyActor")
  myActor ! "test"

  myActor ! "someone"

  system.stop(myActor)
}