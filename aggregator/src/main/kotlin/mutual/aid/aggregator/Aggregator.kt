package mutual.aid.aggregator

import mutual.aid.feature2.AllTheWayDown
import mutual.aid.feature1.FortyTwo
import org.apache.commons.collections4.bag.HashBag

class Aggregator {

  // Simply validating that the report configurations don't interfere with normal classpath
  // configurations
  val allTheWayDown = AllTheWayDown()
  val fortyTwo = FortyTwo()
}

fun main() {
  val agg = Aggregator()
  val bag = HashBag<String>()
  println("Hello world")
}