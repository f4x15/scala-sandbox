package learn

/**
 * Examples of future/promise:
 * https://github.com/anton-k/ru-neophyte-guide-to-scala/blob/master/src/p08-future.md
 */
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Random, Success}

object CoffeeFuture {
  // Определим осмысленные синонимы:
  type CoffeeBeans = String
  type GroundCoffee = String
  case class Water(temperature: Int)
  type Milk = String
  type FrothedMilk = String
  type Espresso = String
  type Cappuccino = String

  // Исключения, на случай если что-то пойдёт не так
  // (они понадобяться нам позже):
  case class GrindingException(msg: String) extends Exception(msg)
  case class FrothingException(msg: String) extends Exception(msg)
  case class WaterBoilingException(msg: String) extends Exception(msg)
  case class BrewingException(msg: String) extends Exception(msg)

  // non-blocking algorithms, that return value "right away" (сразу)
  // Call future-apply:
  //  object Future {
  //    def apply[T](body: => T)(implicit execctx: ExecutionContext): Future[T]    }
  // implicit - неявно подставляется ExecutionContext, думаем про него как пул-потоков
  //  body - как раз то, что должно вычисляться. МОжно передавать как в {} also in ()
  def grind(beans: CoffeeBeans): Future[GroundCoffee] = Future {
    println("start grinding...")
    Thread.sleep(Random.nextInt(200))
    if (beans == "baked beans") throw GrindingException("are you joking?")
    println("finished grinding...")
    s"ground coffee of $beans"
  }

  def heatWater(water: Water): Future[Water] = Future {
    println("heating the water now")
    Thread.sleep(Random.nextInt(2000))
    println("hot, it's hot!")
    water.copy(temperature = 85)
  }

  def frothMilk(milk: Milk): Future[FrothedMilk] = Future {
    println("milk frothing system engaged!")
    Thread.sleep(Random.nextInt(2000))
    println("shutting down milk frothing system")
    s"frothed $milk"
  }

  def brew(coffee: GroundCoffee, heatedWater: Water): Future[Espresso] = Future {
    println("happy brewing :)")
    Thread.sleep(Random.nextInt(2000))
    println("it's brewed!")
    "espresso"
  }


  def main(args: Array[String]): Unit = {

    grind("baked beans").onComplete {
      case Success(ground) => println(s"got my $ground")
      case Failure(ex) => println("This grinder needs a replacement, seriously!")
    }

    Thread.sleep(Random.nextInt(4000))
  }
}

/*
Тип Future[T], определённый в scala.concurrent package — это тип коллекция,
представляющий вычисление, которое когда-нибудь закончится и вернёт значение типа T

Вычисление может закончиться с ошибкой или не буть вычисленным в поставленные временные рамки.
Если что-то пойдёт не так, то результат будет содержать исключение.

Feature is read only-container, when calculate is finished that value is read-only.

Также Future предоставляет методы, позволяющие считать вычисляемое значение.

Запись значения осуществляется с помощью типа Promise.
 */

