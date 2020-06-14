// based on: https://docs.scala-lang.org/ru/tour/traits.html

trait Iterator[A] {
  def hasNext: Boolean
  def next(): A
}

class IntIterator(to: Int) extends Iterator[Int] {
  private var current = 0
  override def hasNext: Boolean = current < to
  override def next(): Int =  {
    if (hasNext) {
      val t = current
      current += 1
      t
    } else 0
  }
}

val iterator = new IntIterator(3)
iterator.next()  // вернет 0
iterator.next()  // вернет 1
iterator.hasNext

iterator.next()

iterator.hasNext

iterator.next()
iterator.next()

/////////////////////////////////////
// sub types

import scala.collection.mutable.ArrayBuffer

trait Pet {
  val name: String
}

class Cat(val name: String) extends Pet
class Dog(val name: String) extends Pet

val dog = new Dog("Harry")
val cat = new Cat("Sally")

val animals = ArrayBuffer.empty[Pet]
animals.append(dog)
animals.append(cat)
animals.foreach(p => println(p.getClass))  // выведет "Harry" и "Sally"