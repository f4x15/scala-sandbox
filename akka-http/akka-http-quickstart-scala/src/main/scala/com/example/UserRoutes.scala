package com.example

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route

import scala.concurrent.Future
import com.example.UserRegistry._
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout

//#import-json-formats
//#user-routes-class
class UserRoutes(userRegistry: ActorRef[UserRegistry.Command])(implicit val system: ActorSystem[_]) {

  //#user-routes-class
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._
  //#import-json-formats

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout =
    Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getUsers(): Future[Users] =
    userRegistry.ask(GetUsers)
  def getUser(name: String): Future[GetUserResponse] =
    userRegistry.ask(GetUser(name, _))
  def createUser(user: User): Future[ActionPerformed] =
    userRegistry.ask(CreateUser(user, _))
  def deleteUser(name: String): Future[ActionPerformed] =
    userRegistry.ask(DeleteUser(name, _))

  //#all-routes
  //#users-get-post
  //#users-get-delete
  // usually route create in separete class
  val userRoutes: Route =
    pathPrefix("users") {     // match incoming request
      concat(   // alias for this is `~`. But concat use for better type-safety
        //#users-get-delete
        pathEnd {   // match the end of the path: here "users" on the end
          concat(   // concatenate two or more route alternatives
                    //  try chain-calculation
            get {   // retrieving all users
              complete(getUsers())  // complete a request and create response from the arguments
            },
            post {  // creating a user
              entity(as[User]) { user =>    // convert request body to domain object of type User
                                            // implicitly we assume that the request contain
                                            // application/json content
                onSuccess(createUser(user)) { performed =>
                  complete((StatusCodes.Created, performed))    // implicitly convert from a tuple
                                                                // `status code` and the text/plain body
                                                                // with the given string
                }
              }
            })
        },
        //#users-get-delete
        //#users-get-post

        // this is common logic
        path(Segment) { name =>   // `Segment` auto extract to the name. And we can get value passed by URI
                                  //  this it is: `/users/$ID`. If we have `/users/Bruce` that populate
                                  //    into name the value Bruce.
                                  // [see more](https://doc.akka.io/docs/akka-http/current/routing-dsl/path-matchers.html?language=scala#basic-pathmatchers)
          concat(
            get {
              //#retrieve-user-info
              rejectEmptyResponse {   // auto unwrap a future, handle an 'Option' by convert into successful
                                      //  response or return 404 status code for `None`
                onSuccess(getUser(name)) { response =>
                  complete(response.maybeUser)  // completes a request which means creating and
                                                // returning a response from the arguments.
                }
              }
              //#retrieve-user-info
            },
            delete {
              //#users-delete-logic
              onSuccess(deleteUser(name)) { performed =>  // here send delete request for user registry actor
                complete((StatusCodes.OK, performed))     //  wait for response and return appropriate
                                                          //  status code for client
              }
              //#users-delete-logic
            })
        })
      //#users-get-delete
    }
  //#all-routes
}
