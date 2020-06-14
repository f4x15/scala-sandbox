// multiple paramaters group function sample

object HelloWorld {
  // define self-defined while 
  def whilst (testCondition: => Boolean)(codeBlock: => Unit): Unit = {
    // in FP-paradigm it must be write with tail-recursion!!!
    while (testCondition) {
      codeBlock
    }
  }

  def ifBothTrue(test1: => Boolean)(test2: => Boolean)(codeBlock: => Unit): Unit = {
    if (test1 && test2) {
      codeBlock
    }
  }

  def main(args: Array[String]): Unit = {
    //println("hello world!")

    // two param example:
    var i = 5
    whilst(i > 3) {
      println(i + "\t")
      i -= 1
    }

    // three param example:
    val age = 19
    val numAccidents = 0
    ifBothTrue (age > 18)(numAccidents == 0) {
      println("Discount")
    }
    ifBothTrue(1 < 2)(3 > 2)(println("hello!"))
  }
}
