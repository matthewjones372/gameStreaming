package GameStreaming
import GameStreaming.Events.FutureEventReader
import GameStreaming.Events.HydrationSource.FileHydrationSource
import GameStreaming.Games.BasketBall.BasketBallEventParser
import GameStreaming.Games.BasketBall.EventFormat.EventFormatV1

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object ExampleApp {

  lazy val parser               = new BasketBallEventParser(EventFormatV1)
  lazy val resourcePath: String = getClass.getResource("/example.txt").getPath
  lazy val hydrationSource      = new FileHydrationSource(resourcePath)
  lazy val eventReader          = new FutureEventReader(hydrationSource, parser)

  def main(args: Array[String]): Unit = {
    eventReader.hydrateBuffer()
    Await.result(eventReader.all, 10.seconds).foreach(println)
  }
}
