package GameStreaming.HydrationSource
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ResourceHydrationSourceTest extends AnyWordSpec with Matchers with TypeCheckedTripleEquals {
  "ResourceHydrationSource" should {
    "return a list of strings from a line separated resource" in {
      val resourceHydrationSource = new ResourceHydrationSource("/line_separated_file.txt")
      resourceHydrationSource.readAll should ===(List("FOO", "BAR", "BIZ"))
    }

    "fail with the resource name when the resource does not exist" in {
      val resourceHydrationSource = new ResourceHydrationSource("/no_such_file.txt")
      the[IllegalArgumentException] thrownBy resourceHydrationSource.readAll should have message
        "Resource not found: /no_such_file.txt"
    }
  }
}
