package com.example.services

import akka.http.scaladsl.server.Directives.{complete, pathEndOrSingleSlash}
import akka.http.scaladsl.server.Route

object MainService {

  def route: Route =
    pathEndOrSingleSlash {
      complete("Welcome to websocket server")
    }
}
