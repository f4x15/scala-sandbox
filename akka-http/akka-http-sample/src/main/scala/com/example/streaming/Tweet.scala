package com.example.streaming

import akka.stream.scaladsl.Source

case class Tweet(uid: Int, txt: String)

object Tweet{
  val tweets = List(
    Tweet(1, "#Akka rocks!"),
    Tweet(2, "Streaming is so hot right now!"),
    Tweet(3, "You cannot enter the same river twice."))
  def getTweets = Source(tweets)    // Tweet streaming
}

object MyTweetJsonProtocol
  extends akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
    with spray.json.DefaultJsonProtocol {

  implicit val tweetFormat = jsonFormat2(Tweet.apply)
}