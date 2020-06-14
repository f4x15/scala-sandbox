// base on: https://alvinalexander.com/scala/concurrency-with-scala-futures-tutorials-examples
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import language.postfixOps

object Futures1 extends App {
  // create async immutable calc
  val f = Future {
    Thread.sleep(500)
    1 + 2
  }

  // block waiting. For demonstrate _only_!!!
  // After this result is guarantied have computated
  //  we can't control this w/o callbacks or Await/Join 
  val result = Await.result(f, 1 second)
  println(s"Result is: $result")

  Thread.sleep(1000)
}
