// in order array "args" call method "foreach"
//  that pass its result to in fuction
//
//  in russian:
//  В отношении массива args вызывается метод foreach
//    передающий результат своей работы в функцию.
//    В данном случае передается в *функциональный литерал*
//    получающий один параметр по имени arg. Телом функции является 
//    вызов println(arg)
//
//  foreach is a iterator
//  applies a function 'f' to all values produced by this iterator.
//  def foreach(f: A => Unit): Unit
args.foreach(arg => println(arg))
//  or as the same ==
  args.foreach(println) // see: functional litheral with one argument and one
  // operation may be - name may be emit
  //
  // funtional literal:   (par1: Int, par2: Int) => par1 + par2
