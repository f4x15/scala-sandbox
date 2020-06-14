// Multiplegenerators sample
// url: https://alvinalexander.com/scala/how-to-use-multiple-generators-scala-for-expressions-loops
object MultipleGenerators extends App {

    val nums = Seq(1,2,3)
    val letters = Seq('a', 'b', 'c')


    // 1 - Seq[(Int, Char)]
    println("\n__ 1 __")
    val res = for {
        n <- nums
        c <- letters
    } yield (n, c)
    res.foreach(a => println("item: " + a))


    // 2 - Seq[List[Char]]
    println("\n__ 2 __")
    val moreLetters = Seq('x', 'y', 'z')
    val res2 = for {
        c1 <- letters
        c2 <- moreLetters
    } yield c1 :: c2 :: Nil
    res2.foreach(a => println("item: " + a))


    // 3
    println("\n__ 3 __")
    val res3 = for {
        n1 <- nums
        n2 <- nums
    } yield (n1, n2)
    res3.foreach(a => println("item: " + a))

}
