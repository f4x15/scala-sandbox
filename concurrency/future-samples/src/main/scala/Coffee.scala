package learn

import scala.util.{Failure, Success, Try}

/**
 * Examples of future/promise:
 * https://github.com/anton-k/ru-neophyte-guide-to-scala/blob/master/src/p08-future.md
 *
 * sequence-blocked algorithm sample
 */
object Coffee {
  // Определим осмысленные синонимы:
  type CoffeeBeans = String
  type GroundCoffee = String

  case class Water(temperature: Int)

  type Milk = String
  type FrothedMilk = String
  type Espresso = String
  type Cappuccino = String

  // Методы-заглушки для отдельных шагов алгоритма:
  def grind(beans: CoffeeBeans): GroundCoffee = s"ground coffee of $beans"

  def heatWater(water: Water): Water = water.copy(temperature = 85)

  def frothMilk(milk: Milk): FrothedMilk = s"frothed $milk"

  def brew(coffee: GroundCoffee, heatedWater: Water): Espresso = "espresso"

  def combine(espresso: Espresso, frothedMilk: FrothedMilk): Cappuccino = "cappuccino"

  // Исключения, на случай если что-то пойдёт не так
  // (они понадобяться нам позже):
  case class GrindingException(msg: String) extends Exception(msg)

  case class FrothingException(msg: String) extends Exception(msg)

  case class WaterBoilingException(msg: String) extends Exception(msg)

  case class BrewingException(msg: String) extends Exception(msg)

  // Try - success or exception computation
  // for-comprehension
  def prepareCappuccino(): Try[Cappuccino] = for {
    ground <- Try(grind("arabica beans"))
    water <- Try(heatWater(Water(25)))
    espresso <- Try(brew(ground, water))
    foam <- Try(frothMilk("milk"))
  } yield combine(espresso, foam)


  def main(args: Array[String]): Unit = {

    val result = prepareCappuccino()

    result match {
      case Success(cap) => println(cap)
      case Failure(e) => println(s"error: $e")
    }
  }
}
