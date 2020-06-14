package com.example

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}

object ZipWithSample extends App {

  implicit val system = ActorSystem("ZipWithSample")

  val sourceCount = Source(List("one", "two", "three"))
  val sourceFruits = Source(List("apple", "orange", "banana"))

  // Combines elements from multiple sources through a combine function and
  //  passes the returned value downstream.
  sourceCount
    .zipWith(sourceFruits) { (countStr, fruitName) =>
      s"$countStr $fruitName"
    }
    .runWith(Sink.foreach(println))

}
