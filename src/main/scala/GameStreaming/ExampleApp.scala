package GameStreaming
import GameStreaming.EventFormat.EventFormatV1
import GameStreaming.HydrationSource.ResourceHydrationSource

object AppLoader {
  private lazy val parser               = new BasketballEventParser(EventFormatV1)
  private lazy val hydrationSource      = new ResourceHydrationSource("/example.txt")
  lazy val eventReader                  = new IdEventReader(hydrationSource, parser)
}

object ExampleApp {
  import AppLoader._

  def main(args: Array[String]): Unit = {
    eventReader.hydrateBuffer()
    eventReader.all.foreach(println)
  }
}
