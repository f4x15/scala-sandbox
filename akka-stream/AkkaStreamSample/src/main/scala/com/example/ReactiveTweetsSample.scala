package com.example

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.{ClosedShape, OverflowStrategy}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, RunnableGraph, Sink, Source}
import com.example
import javax.net.ssl.SSLEngineResult.HandshakeStatus
import javax.security.auth.callback.CallbackHandler

import scala.concurrent.{ExecutionContext, Future}

/**
 * Sample about `backpressure`:
 *  Answer on question: "What if the subscriber is too slow to consume the live stream of data?"
 */
object ReactiveTweetsSample extends App {
  import example.Author._
  import example.Hashtag._
  import example.Tweet._

  implicit val system = ActorSystem("reactive-tweets")

  //<editor-fold desc="Prepare some-one data in other place">

  val tweets: Source[Tweet, NotUsed] = Source(
    Tweet(Author("rolandkuhn"), System.currentTimeMillis, "#akka rocks!") ::
      Tweet(Author("patriknw"), System.currentTimeMillis, "#akka !") ::
      Tweet(Author("bantonsson"), System.currentTimeMillis, "#akka !") ::
      Tweet(Author("drewhk"), System.currentTimeMillis, "#akka !") ::
      Tweet(Author("ktosopl"), System.currentTimeMillis, "#akka on the rocks!") ::
      Tweet(Author("mmartynas"), System.currentTimeMillis, "wow #akka !") ::
      Tweet(Author("akkateam"), System.currentTimeMillis, "#akka rocks!") ::
      Tweet(Author("bananaman"), System.currentTimeMillis, "#bananas rock!") ::
      Tweet(Author("appleman"), System.currentTimeMillis, "#apples rock!") ::
      Tweet(Author("drama"), System.currentTimeMillis, "we compared #apples to #oranges!") ::
      Nil)
  //</editor-fold>

  val akkaTag = Hashtag("#akka")  // for sample only
  val authors: Source[Author, NotUsed] =
    tweets.filter(_.hashtags.contains(akkaTag)).map(_.author)

  // To materialize and run we need attach the flow to Sink that will get Flow running
  // simplest way for it call `runWith(sink)` on a Source
  // There are some predefined `Sinks` in Sink companion object
  authors.runWith(Sink.foreach(println))  // run

  // or in the shortest-way
  // Exist for common: Sink.fold and Sink.foreach etc
  authors.runForeach(println)

  // flat sample
  val hashtags: Source[Hashtag, NotUsed] = tweets.mapConcat(_.hashtags.toList)

  //<editor-fold desc="Broadcasting the stream: split into pieces">
  // Task: create two output flows for write in files Authors and Hashtags

  // for create fan-out and fan-in structures we need create the GraphDSL
  val writeAuthors: Sink[Author, Future[Done]] = Sink.ignore
  val writeHashtags: Sink[Hashtag, Future[Done]] = Sink.ignore

  val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit b => // call `b` GraphBuilder
    import GraphDSL.Implicits._

    val bcast = b.add(Broadcast[Tweet](2))
    tweets ~> bcast.in
    bcast.out(0) ~> Flow[Tweet].map(_.author) ~> writeAuthors
    bcast.out(1) ~> Flow[Tweet].mapConcat(_.hashtags.toList) ~> writeHashtags
    ClosedShape   // ClosedShape means that it is a fully connected graph
  })
  g.run()

  //<editor-fold desc="Backpressure in Action. Custom bubber size">

  // backpressure enabmes by-default, but ca also handle buffer-size
  tweets.buffer(10, OverflowStrategy.dropHead).map(t => t.timestamp * t.timestamp ).runWith(Sink.ignore)
  // this is some fat-computation
  // `dropHead` - droping the oldest element
  //</editor-fold>

  //<editor-fold desc="Materialized values for infinity streams">

  // reusable Flow for change incoming tweet into an integer value 1
  val count: Flow[Tweet, Int, NotUsed] = Flow[Tweet].map(_ => 1)

  // We’ll count Flow to combine those with a Sink.fold that will sum all Int elements
  //  of the stream and make its result available as a Future[Int]
  val sumSink: Sink[Int, Future[Int]] = Sink.fold[Int, Int](0)(_ + _)

  // Next we connect the tweets stream to count with via.
  //  Finally we connect the Flow to the previously prepared Sink using toMat.
  val counterGraph: RunnableGraph[Future[Int]] =
    tweets.via(count).toMat(sumSink)(Keep.right)

  val sum: Future[Int] = counterGraph.run()

  // sum.foreach(c => println(s"Total tweets processed: $c"))
  // "count elements on finite stream"


  //<editor-fold desc="Reuse graph system for call">
  /*
  val sumSink = Sink.fold[Int, Int](0)(_ + _)
  val counterRunnableGraph: RunnableGraph[Future[Int]] =
      contains akkaTag).map(t => 1).toMat(sumSink)(Keep.right)

  // materialize the stream once in the morning
  val morningTweetsCount: Future[Int] = counterRunnableGraph.run()
  // and once in the evening, reusing the flow
  val eveningTweetsCount: Future[Int] = counterRunnableGraph.run()
   */
  //</editor-fold>



  //</editor-fold>

  //</editor-fold>

}

// Assume we have tweet-source
abstract class TweetSourceDecl {
  //#tweet-source
  val tweets: Source[Tweet, NotUsed]
  //#tweet-source
}

//<editor-fold desc="Reactive tweet domain model">
final case class Author(handle: String)

final case class Hashtag(name: String)

final case class Tweet(author: Author, timestamp: Long, body: String) {
  def hashtags =
    body
      .split(" ")
      .collect {
        case t if t.startsWith("#") => Hashtag(t.replaceAll("[^#\\w]", ""))
      }
      .toSet
}
//</editor-fold>

