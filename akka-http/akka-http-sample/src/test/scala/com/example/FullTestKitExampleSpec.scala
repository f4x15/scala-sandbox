package com.example

import org.scalatest.{ Matchers, WordSpec }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.server._
import Directives._

// for test-timeouts
import scala.concurrent.duration._
import akka.http.scaladsl.testkit.RouteTestTimeout
import akka.testkit.TestDuration

/**
 * Example test kit
 * @see <a href="https://doc.akka.io/docs/akka-http/current/routing-dsl/testkit.html">TestKit sample</a>
 */
class FullTestKitExampleSpec extends WordSpec with Matchers with ScalatestRouteTest {

  // for test-timeouts, default 1 second, change to 5 seconds
  implicit val timeout = RouteTestTimeout(5.seconds.dilated)

  // example route for test
  val smallRoute =
    get {
      concat(
        pathSingleSlash {
          complete {
            "Captain on the bridge!"
          }
        },
        path("ping") {
          complete("PONG!")
        }
      )
    }

  /*
  Common route test template:
  REQUEST ~> ROUTE ~> check {
    ASSERTIONS
  }
   */

  "The service" should {

    "return a greeting for GET requests to the root path" in {
      Get() ~> smallRoute ~> check {
        responseAs[String] shouldEqual "Captain on the bridge!"
      }
    }

    "return a 'PONG!' response for GET requests to /ping" in {
      Get("/ping") ~> smallRoute ~> check {
        responseAs[String] shouldEqual "PONG!"
      }
    }

    "leave GET requests to other paths unhandled" in {
      Get("/kermit") ~> smallRoute ~> check {
        handled shouldBe false
      }
    }

    "return a MethodNotAllowed error for PUT requests to the root path" in {
      Put() ~> Route.seal(smallRoute) ~> check {    // Route.seat for testing Unhandled route
        status shouldEqual StatusCodes.MethodNotAllowed
        responseAs[String] shouldEqual "HTTP method not allowed, supported methods: GET"
      }
    }
  }
}
