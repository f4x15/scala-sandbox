// Demonstarate principle: Function are variables
// based on: 
object FunctionAreVariables {

  def main(args: Array[String]): Unit = {
    
    // name: type = value
    val name: String = "Vasya"

    // this is "function literal"
    // name = type => value
    val double = (i: Int) => i * 2

    // named finction literal
    // name = type => value/body
    val isEven = (i: Int) => i % 2 == 0

    // may me long-syntax 
    val isEven2 = (i: Int) => { i % 2 == 0 }

    // or very-long syntax. But it isn't Scala-way
    val isEven3 = (i: Int) => { if (i % 2 == 0) true else false }

    val sum = (a: Int, b: Int) => a + b
    /*
    The type of the isEven function can be read as, “Transforms an Int value into a Boolean value,” and the sum function can be read as, “Takes two Int input parameters and transforms them into an Int.”

Cool FP like say the same: “a function transforms its inputs into an output value.”
*/

  

  }

}
