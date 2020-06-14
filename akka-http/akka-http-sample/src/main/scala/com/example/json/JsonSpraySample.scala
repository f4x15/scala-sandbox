package com.example.json

// import for Implicits converts
import spray.json.DefaultJsonProtocol._
import spray.json._ // if you don't supply your own Protocol (see below)

/**
 * Samples of JSON-Spray
 * @see <a href="https://doc.akka.io/docs/akka-http/current/common/json-support.html"official documentations</a>
 */
object JsonSpraySample extends App{

  // convert to JSON
  val source = """{ "some": "JSON source" }"""
  val jsonAst = source.parseJson // or JsonParser(source)
  println(jsonAst)

  val json = jsonAst.prettyPrint // or .compactPrint
  println(json)

  // convert any Scala object to JSON AST
  val listJsonAst = List(1, 2, 3).toJson
  println(listJsonAst)

  // convert from JSON
  val myObj = listJsonAst.convertTo[List[Int]]
  println(myObj)
}
