package GameStreaming.Events

import GameStreaming.Events.HydrationSource.{FileHydrationSource, HydrationSource}
import GameStreaming.Games.BasketBall.BasketBallEventParser
import GameStreaming.Games.BasketBall.BasketBallPoint.{OnePointer, ThreePointer, TwoPointer}
import GameStreaming.Games.BasketBall.BasketballEvent.TeamScored
import GameStreaming.Games.BasketBall.BasketballTeam.{Team1, Team2}
import GameStreaming.Games.BasketBall.EventFormat.EventFormatV1
import GameStreaming.Utils.TeamScoreTesting._
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.OptionValues
import org.scalatest.compatible.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class IdEventReaderTest extends AnyWordSpec with Matchers with TypeCheckedTripleEquals with OptionValues {
  "IdEventReader" should {
    "return none when there are no events" in {
      lastShouldMatch("/empty_file.txt", None)
    }

    "return the last valid event from a file with valid events" in {
      lastShouldMatch("/with_valid_event.txt", Some(TeamScored(ThreePointer, Team2, expectedGameState(2, 3, 30))))
    }

    "return the last valid event from a file with an invalid event" in {
      lastShouldMatch("/with_last_invalid.txt", Some(TeamScored(ThreePointer, Team2, expectedGameState(2, 3, 30))))
    }

    "return the last n events" in {
      lastNEventsShouldMatch(
        "/with_multiple_events.txt",
        5,
        List(
          TeamScored(TwoPointer, Team2, expectedGameState(21, 28, 499)),
          TeamScored(TwoPointer, Team1, expectedGameState(23, 28, 533)),
          TeamScored(OnePointer, Team2, expectedGameState(23, 29, 559)),
          TeamScored(TwoPointer, Team1, expectedGameState(25, 29, 581)),
          TeamScored(TwoPointer, Team1, expectedGameState(27, 29, 598))
        )
      )
    }

    "does not return inconsistent values" in {
      val eventParser = new BasketBallEventParser(EventFormatV1)
      val fileEventReader = new IdEventReader(EmptyHydrationSource, eventParser)

      val firstEvent = "0x29f981a2"
      val secondEventWithLowerScores = "0x781002"

      //Add the first event
      fileEventReader.add(firstEvent)
      fileEventReader.add(secondEventWithLowerScores)

      fileEventReader.all should have size 1
      fileEventReader.all.head should ===(TeamScored(TwoPointer, Team1, expectedGameState(48, 52, 1343)))
    }
  }

  private val eventParser = new BasketBallEventParser(EventFormatV1)

  private def lastShouldMatch(filePath: String, expected: Option[TeamScored]): Assertion = {
    val path = getClass.getResource(filePath).getPath
    val fileHydrationSource = new FileHydrationSource(path)
    val fileEventReader = new IdEventReader(fileHydrationSource, eventParser)
    fileEventReader.hydrateBuffer()
    fileEventReader.last should ===(expected)
  }

  private def lastNEventsShouldMatch(filePath: String, n: Int, expected: List[TeamScored]): Assertion = {
    val path = getClass.getResource(filePath).getPath
    val fileHydrationSource = new FileHydrationSource(path)
    val fileEventReader = new IdEventReader(fileHydrationSource, eventParser)
    fileEventReader.hydrateBuffer()
    fileEventReader.lastN(n) should have size n
    fileEventReader.lastN(n) should ===(expected)
  }

  object EmptyHydrationSource extends HydrationSource[String] {
    override def readAll: List[String] = List.empty[String]
  }
}
