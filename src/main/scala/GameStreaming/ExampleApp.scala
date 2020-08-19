package GameStreaming
import GameStreaming.EventFormat.EventFormatV1
import GameStreaming.HydrationSource.FileHydrationSource

object AppLoader {
  private lazy val parser               = new BasketBallEventParser(EventFormatV1)
  private lazy val resourcePath: String = getClass.getResource("/example.txt").getPath
  private lazy val hydrationSource      = new FileHydrationSource(resourcePath)
  lazy val eventReader                  = new IdEventReader(hydrationSource, parser)
}

object ExampleApp {
  import AppLoader._

  def main(args: Array[String]): Unit = {
    eventReader.hydrateBuffer()
    eventReader.all.foreach(println)
  }
}
