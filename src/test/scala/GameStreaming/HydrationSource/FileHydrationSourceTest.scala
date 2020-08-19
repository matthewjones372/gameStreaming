package GameStreaming.HydrationSource
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class FileHydrationSourceTest extends AnyWordSpec with Matchers with TypeCheckedTripleEquals {
  "FileHydrationSource" should {
    "return a list of strings from a valid line separated file" in {
      val fileHydrationSource = new FileHydrationSource(getClass.getResource("/line_separated_file.txt").getPath)
      fileHydrationSource.readAll should ===(List("FOO", "BAR", "BIZ"))
    }
  }
}
