package GameStreaming.Events

import GameStreaming.Games.BasketBall.BasketBallPoint.{OnePointer, ThreePointer, TwoPointer}
import GameStreaming.Games.BasketBall.BasketballTeam.{Team1, Team2}
import GameStreaming.Games.BasketBall.EventFormat.EventFormatV1
import GameStreaming.Games.BasketBall.TeamScored.NonConsistentState
import GameStreaming.Games.BasketBall.{BasketBallEventParser, TeamScored}
import GameStreaming.Utils.TeamScoreTesting._
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.OptionValues
import org.scalatest.compatible.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import scala.concurrent.Future

class FileEventReaderTest extends AsyncWordSpec with Matchers with TypeCheckedTripleEquals with OptionValues {
  "FileEventReader" should {
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
      val expectedEvents = List(
        TeamScored(TwoPointer, Team2, expectedGameState(21, 28, 499)),
        TeamScored(TwoPointer, Team1, expectedGameState(23, 28, 533)),
        TeamScored(OnePointer, Team2, expectedGameState(23, 29, 559)),
        TeamScored(TwoPointer, Team1, expectedGameState(25, 29, 581)),
        TeamScored(TwoPointer, Team1, expectedGameState(27, 29, 598))
      )

      lastNEventsShouldMatch("/with_multiple_events.txt", 5, expectedEvents)
    }

    "can add events which are consistent and non consistent events are flagged" in {
      val eventParser = new BasketBallEventParser(EventFormatV1)
      val fileEventReader = new FileEventReader("/empty_file.txt", eventParser)

      ScalaCheckDrivenPropertyChecks.forAll(teamScoreGen) { newTeamScore: TeamScored =>
        fileEventReader.last.flatMap { lastTeamScore: Option[TeamScored] =>
        fileEventReader.addEvent(newTeamScore.toHex) flatMap { eventAdded =>
          if(newTeamScore.isConsistentWith(lastTeamScore.value)){
            eventAdded should ===(Right(true))
          } else {
            eventAdded should ===(Left(NonConsistentState("Non")))
          }
        }
        }
      }

      fileEventReader.all flatMap{ events =>
        events should have size 10
      }
    }
  }

  private val eventParser = new BasketBallEventParser(EventFormatV1)


  private def lastShouldMatch(filePath: String, expected: Option[TeamScored]): Future[Assertion] = {
    val path            = getClass.getResource(filePath).getPath
    val fileEventReader = new FileEventReader(path, eventParser)
    fileEventReader.hydrateBuffer()
    fileEventReader.last flatMap { event =>
      event should ===(expected)
    }
  }

  private def lastNEventsShouldMatch(filePath: String, n: Int, expected: List[TeamScored]): Future[Assertion] = {
    val path            = getClass.getResource(filePath).getPath
    val fileEventReader = new FileEventReader(path, eventParser)
    fileEventReader.hydrateBuffer()
    fileEventReader.lastN(n) flatMap { events =>
      events should have size n
      events should ===(expected)
    }
  }
}
