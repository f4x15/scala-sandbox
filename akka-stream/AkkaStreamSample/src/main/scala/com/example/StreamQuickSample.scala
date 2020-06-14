package com.example

import java.nio.file.Paths

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import akka.util.ByteString

import scala.concurrent.Future
import scala.concurrent.duration._

/**
 * Classic akka-stream quick start
 * @see <a href="https://doc.akka.io/docs/akka/current/stream/stream-quickstart.html">based on</a>
 */
object StreamQuickSample {
  def main(args: Array[String]): Unit = {
    //<editor-fold desc="Create Actor-system">

    implicit val system = ActorSystem("StreamQuickSample")
    //</editor-fold>

    //<editor-fold desc="Create simple `Sink`">

    // create  simple`Source` what emitting range 1..100
    // first parameter - out
    // the second - `materialized value` - allows produce some auxiliary value, if
    //  not need - use `NotUsed`
    val source: Source[Int, NotUsed] = Source(1 to 100)

    // run the source
    val done: Future[Done] = source.runForeach(i => println(i))
    // we add `consumer` functions to `source` and pass the little stream setup to
    //  an Actor that runs it.
    //  This activation is signaled by having “run” be part of the method name.
    //</editor-fold>

    //<editor-fold desc="Write `Sink` to file">

    // we may reuse source: Range 1..100
    val factorials = source.scan(BigInt(1))((acc, next) => acc * next)  // calculate factorial
    // we can use `scan` operator to computation over the whole stream.
    // this is nothing computed yet, it is blueprint only

    val result: Future[IOResult] =
    // convert result series of integers into stream of `ByteString` objects describing lines in a file
    // This stream is then run by attaching a file as receiver of the data. In AS terminology it is
    //  this is called a `Sink`.
      factorials.map(num => ByteString(s"$num\n")).
        runWith(FileIO.toPath(Paths.get("factorials.txt")))
    // `runWith` - connect `Source` to `Sink` and run it.
    //</editor-fold>

    //<editor-fold desc="Reuse `Sink` for write in a file">

    // In contrast to other libraries we can `reuse Sink`: create some specific sink and than use its.
    factorials.map(_.toString).runWith(lineSink("factorial2.txt"))
    //</editor-fold>

    //<editor-fold desc="Throttle sample">
    factorials
      .zipWith(Source(0 to 100))((num, idx) => s"$idx! = $num") // combine elements from multiple src
      .throttle(1, 1.second)  // throttle the flow throw send to up-down information
      .runForeach(println)
    //</editor-fold>

    /*
    //<editor-fold desc="Terminate">

    // because actor not finished we need terminate by end of operation
    implicit val ec = system.dispatcher
    done.onComplete(_ => system.terminate()) // when source emit is end we will stop the system
    //</editor-fold>
  */
  }

  // accepts strings as its input and when `materialized it` will create `auxiliary information`
  //  of type Future[IOResult]
  def lineSink(filename: String): Sink[String, Future[IOResult]] =
    // `Flow` from left to right
    Flow[String].map(s => ByteString(s + "\n")).toMat(FileIO.toPath(Paths.get(filename)))(Keep.right)
  // create some auxiliary infaormation - matirualized some one.
  // since we want to retain what the FileIO.toPath sink has to offer, we need to say Keep.right
}


















