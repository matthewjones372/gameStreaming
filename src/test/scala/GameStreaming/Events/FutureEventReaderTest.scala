package GameStreaming.Events

import GameStreaming.Events.HydrationSource.{ FileHydrationSource, HydrationSource }
import GameStreaming.Games.BasketBall.BasketBallPoint.{ OnePointer, ThreePointer, TwoPointer }
import GameStreaming.Games.BasketBall.BasketballTeam.{ Team1, Team2 }
import GameStreaming.Games.BasketBall.EventFormat.EventFormatV1
import GameStreaming.Games.BasketBall.TeamScored.NonConsistentState
import GameStreaming.Games.BasketBall.{ BasketBallEventParser, TeamScored }
import GameStreaming.Utils.TeamScoreTesting._
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.OptionValues
import org.scalatest.compatible.Assertion
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.Configuration.minSuccessful
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.scalacheck.{ ScalaCheckDrivenPropertyChecks => sc }

import scala.concurrent.Future

class FutureEventReaderTest
    extends AsyncWordSpec
    with Matchers
    with TypeCheckedTripleEquals
    with OptionValues
    with Eventually {
  "FutureEventReader" should {
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
      val eventParser     = new BasketBallEventParser(EventFormatV1)
      val fileEventReader = new FutureEventReader(EmptyHydrationSource, eventParser)

      val firstEvent                 = "0x29f981a2"
      val secondEventWithLowerScores = "0x781002"

      //Add the first event
      fileEventReader.add(firstEvent).map { _ =>
        //then the second event
        fileEventReader.add(secondEventWithLowerScores)
      }

      eventually {
        fileEventReader.all.flatMap { result =>
          result should have size 1
          result.head should ===(TeamScored(TwoPointer, Team1, expectedGameState(48, 52, 1343)))
        }
      }
    }

    "can add events which are consistent and non consistent events are flagged" in sc.forAll(minSuccessful(500)) { _ =>
      val eventParser     = new BasketBallEventParser(EventFormatV1)
      val fileEventReader = new FutureEventReader(EmptyHydrationSource, eventParser)

      sc.forAll(teamScoreGen) { newTeamScore: TeamScored =>
        fileEventReader.last.map { lastTeamScore: Option[TeamScored] =>
          fileEventReader.add(newTeamScore.toHexString) map { eventAdded =>
            if (newTeamScore.isConsistentWith(lastTeamScore))
              eventAdded should ===(Right(true))
            else
              eventAdded should ===(Left(NonConsistentState("Non")))
          }
        }
      }

      fileEventReader should ===(fileEventReader)
    }
  }

  private val eventParser = new BasketBallEventParser(EventFormatV1)

  private def lastShouldMatch(filePath: String, expected: Option[TeamScored]): Future[Assertion] = {
    val path                = getClass.getResource(filePath).getPath
    val fileHydrationSource = new FileHydrationSource(path)
    val fileEventReader     = new FutureEventReader(fileHydrationSource, eventParser)
    fileEventReader.hydrateBuffer()
    eventually(fileEventReader.last map { event =>
      event should ===(expected)
    })
  }

  private def lastNEventsShouldMatch(filePath: String, n: Int, expected: List[TeamScored]): Future[Assertion] = {
    val path                = getClass.getResource(filePath).getPath
    val fileHydrationSource = new FileHydrationSource(path)
    val fileEventReader     = new FutureEventReader(fileHydrationSource, eventParser)
    fileEventReader.hydrateBuffer()
    eventually(fileEventReader.lastN(n) map { events =>
      events should have size n
      events should ===(expected)
    })
  }

  object EmptyHydrationSource extends HydrationSource[String] {
    override def readAll: List[String] = List.empty[String]
  }
}
