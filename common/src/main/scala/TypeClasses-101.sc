// based on: https://alvinalexander.com/scala/fp-book/type-classes-101-introduction/

/*
# Type classes
  In FP if we need add/update some behaviour we create type-classes.

  ## Component of TC
  TC contains three component:
  - The type classes defined as trait that take at list one "generic" parameter.
  - Instance of the type class for types you want to extend
  - Interface methods that expose new API to users

 */

sealed trait Animal
final case class Dog(name: String) extends Animal
final case class Cat(name: String) extends Animal
final case class Bird(name: String) extends Animal

// we can add some function or add Type-classes for example

// step 1: add Trait with generic
trait BehavesLikeHuman[A] {
  def speak(a: A): Unit
}

// step 2: add type-class instance
object BehavesLikeHumanInstances {
  // only for `Dog`
  implicit val dogBehavesLikeHuman = new BehavesLikeHuman[Dog] {
    def speak(dog: Dog): Unit = {
      println(s"I'm a Dog, my name is ${dog.name}")
    }
  }
}

// step 3: The API interface
//  we can have 3.1 or 3.2 implementation there are competition realisations

// step 3.1: The Interface Objects approach
object BehavesLikeHuman {
  /**
   *
   * @param a
   * @param behavesLikeHumanInstance - concrete behaviour instance
   * @tparam A - generic parameter
   */
  def speak[A](a: A)(implicit behavesLikeHumanInstance: BehavesLikeHuman[A]): Unit = {
    behavesLikeHumanInstance.speak(a)   // concrete instance
  }
}

// step 3.1 consumer:
import BehavesLikeHumanInstances.dogBehavesLikeHuman
val rover = Dog("Rover")

BehavesLikeHuman.speak(rover)   // apply our behaviour
BehavesLikeHuman.speak(rover)(dogBehavesLikeHuman)  // also you can manually call

// step 3.2: The Interface Syntax approach  (Catz library style)
object BehavesLikeHumanSyntax {
  implicit class BehavesLikeHumanOps[A](value: A) {
    def speak(implicit behavesLikeHumanInstance: BehavesLikeHuman[A]): Unit = {
      behavesLikeHumanInstance.speak(value)
    }
  }
}

// use of step 3.2
import BehavesLikeHumanInstances.dogBehavesLikeHuman
import BehavesLikeHumanSyntax.BehavesLikeHumanOps

val rover2 = Dog("Rover")
rover2.speak

