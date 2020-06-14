import org.scalatest._

class SimplestPossibleSpec extends FlatSpec {

  "An empty Set" should "have size 0" in {
    assert(Set.empty.size == 0)
  }
}

