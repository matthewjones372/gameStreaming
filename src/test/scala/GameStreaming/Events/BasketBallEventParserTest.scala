package GameStreaming.Events

import GameStreaming.Games.BasketBall.BasketBallEventParser.{ InvalidEventString, InvalidScore }
import GameStreaming.Games.BasketBall.BasketBallPoint._
import GameStreaming.Games.BasketBall.BasketballTeam._
import GameStreaming.Games.BasketBall.TeamScored
import GameStreaming.Utils.TeamScoreTesting._
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.{ Configuration, TableFor3 }
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.{ ScalaCheckDrivenPropertyChecks => sc }

class BasketBallEventParserTest extends AnyWordSpec with Matchers with TypeCheckedTripleEquals with Configuration {
  "BasketBallEventParser" should {
    "parse valid event examples" in {
      val validEventExamples: TableFor3[String, String, TeamScored] = Table(
        ("Scenario", "Event Hex", "Expected Event Decoded"),
        (
          "15 seconds of play, Team 1 scores 2 points",
          "0x781002",
          TeamScored(TwoPointer, Team1, expectedGameState(2, 0, 15))
        ),
        (
          "30 seconds of play, Team 2 scores  3 points",
          "0xf0101f",
          TeamScored(ThreePointer, Team2, expectedGameState(2, 3, 30))
        ),
        (
          "At 10:10 game time, Team 1 scores 1 point",
          "0x1310c8a1",
          TeamScored(OnePointer, Team1, expectedGameState(25, 20, 610))
        ),
        (
          "At 22:23 game time, Team 1 scores 2 points",
          "0x29f981a2",
          TeamScored(TwoPointer, Team1, expectedGameState(48, 52, 1343))
        ),
        (
          "At 38:30 game time, Team 2 scores 3 points",
          "0x48332327",
          TeamScored(ThreePointer, Team2, expectedGameState(100, 100, 2310))
        )
      )

      forAll(validEventExamples) {
        case (scenario, event, expected) =>
          withClue(scenario) {
            eventParser.parseEvent(event) should ===(Right(expected))
          }
      }
    }

    "parse valid events with values in the minimum and maximum range" in sc.forAll(minSuccessful(500)) { _ =>
      sc.forAll(teamScoreGen) { event =>
        withClue("Could not parse event with valid values") {
          eventParser.parseEvent(event.toHexString) should ===(Right(event))
        }
      }
    }

    "return an InvalidEventString error when given an invalid event string" in {
      eventParser.parseEvent("SOME_INVALID_STRING") should ===(
        Left(InvalidEventString("Could not parse input event: SOME_INVALID_STRING"))
      )
    }

    "return an GameScoreParseError when given an invalid score event" in {
      //An invalid event with a score of 0
      val invalidEvent = "0x781000"
      eventParser.parseEvent(invalidEvent) should ===(Left(InvalidScore("Decoded points scored: 0 is not possible")))
    }
  }
}
