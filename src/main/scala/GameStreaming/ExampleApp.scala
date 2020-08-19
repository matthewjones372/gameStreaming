package GameStreaming
import GameStreaming.Events.HydrationSource.FileHydrationSource
import GameStreaming.Events.IdEventReader
import GameStreaming.Games.BasketBall.BasketBallEventParser
import GameStreaming.Games.BasketBall.EventFormat.EventFormatV1

object ExampleApp {

  lazy val parser = new BasketBallEventParser(EventFormatV1)
  lazy val resourcePath: String = getClass.getResource("/example.txt").getPath
  lazy val hydrationSource = new FileHydrationSource(resourcePath)
  lazy val eventReader = new IdEventReader(hydrationSource, parser)

  def main(args: Array[String]): Unit = {
    eventReader.hydrateBuffer()
    eventReader.all.foreach(println)
  }
}

// TODO: add a more interesting example add using add
// TODO: Write in-depth read me
