// itn't type Scala-style syntax!!!
//  Array[String] parametriz by-type
//  (3) paramaetriz by-val
val helloStrings = new Array[String](3)

// array is *val*, but ELEMTS of array is mutable (var)
//  array *helloString* it is always `Array[String]` val
//  but may mutate it elements
helloStrings(0) = "Hello"
helloStrings.update(1, "dear")  // update translate into 'update'
helloStrings(2) = " fried"

// *to* is operator
// "0 to 2" it is equvalent "(0).to(2)"
// Scala haven't operators and haven't polimorhism because
// *all operations are method-calls*
for (i <- 0 to 2) {         // () translate into `apply` method
  println(helloStrings.apply(i))
}

val numbers = Array(1, 2, 3)
// is are equalent is:
// *Array.apply* is companion-object ("спутник") 
//  as static methods in Java
//  Apply when `method()`
val numbers2 = Array.apply(1, 2, 3)

val oneTwoThre = List(1, 2, 3)

// ::: concatinate lists
val oneTwoThreFour = List(1, 2) ::: List(3, 4)

// cons is frequency used operator with Lists
// add in begin existed list element and return new list
val _1234 = 1 :: List(2,3,4)

// "Nil' is const of empty list
//  Way for create new list:  
val _123 = 1 :: 2 :: 3 :: Nil

// methods of list:
val thrill = "Will" :: "fill" :: "untill" :: Nil

// return new list length of 4 
thrill.filter(s => s.length == 4)

// carry oit predicate for all list elements?
thrill.forall(s => s.endWith("l"))

// print all elements. W/o oupput for inpure function
thrill.foreach(s => print(s))

// return modified list list 
thrill.map(s >= s + "y")

// ---------- Tuple - "кортеж" in russian
// Tuples can contain elements by various type VS 
//  List with single elements type
var pair = (11, "I am tuple")
println(pair._1)
println(pair._2)






