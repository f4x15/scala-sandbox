object ThreadSample extends App {
  val hello = new Thread(new Runnable {
  def run() {
    println("hello world from new Thread" + )
  }
  })
}
