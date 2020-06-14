package com.example

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl._

final case class Author(handle: String)

final case class Hashtag(name: String)

final case class Tweet(author: Author, timestamp: Long, body: String) {
  def hashtags: Set[Hashtag] =
    body
      .split(" ")
      .collect {  // builds a new collection by applying a partial function to all elements of this
                  // sequence on which the function is define
        case t if t.startsWith("#") => Hashtag(t.replaceAll("[^#\\w]", ""))
                                      // remote if isn't #hastag:
                                      // [^] - not in brackets. .In brakets - hashtag
      }
      .toSet
}

object TweetsSample extends App {
  val akkaTag = Hashtag("#akka")

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

  implicit val system = ActorSystem("reactive-tweets")

  //println(tweets.runWith(Sink.foreach(println)))    // simple println

  println(tweets.
    map(_.hashtags).    // get all sets of hashtags
    reduce(_++_).       // reduce them to single set, remove duplicates
    mapConcat(identity) // Flatten the set of hashtags to a stream of hashtags
    map(_.name.toUpperCase) // Convert all hashtags to upper case
    runWith(Sink.foreach(println)))     // run and print on screen
}