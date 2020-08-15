package Domain.Games

import Games.BasketBall.BasketBallPoint._
import Games.BasketBall.BasketballTeam._
import Games.BasketBall.FormatSpecification.EventFormatV1
import Games.BasketBall.{BasketBallEventParser, GameState, TeamScored}
import eu.timepit.refined.types.numeric.NonNegInt
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.TableFor2

class BasketBallEventParserTest extends AnyFlatSpec with Matchers with TypeCheckedTripleEquals {

  val validEvents: TableFor2[String, TeamScored] = Table(
    ("Event Hex", "Expected Event Decoded"),
    ("0x781002", TeamScored(TwoPointer, Team1, expectedGameState(2, 0, 15))),
    ("0xf0101f", TeamScored(ThreePointer, Team2, expectedGameState(2, 3, 30))),
    ("0x1310c8a1", TeamScored(OnePointer, Team1, expectedGameState(25, 20, 610))),
    ("0x29f981a2", TeamScored(TwoPointer, Team1, expectedGameState(48, 52, 1343))),
    ("0x48332327", TeamScored(ThreePointer, Team2, expectedGameState(100, 100, 2310)))
  )

  "BasketBallEventParser" should "parse valid events" in {
    val eventParser = new BasketBallEventParser(EventFormatV1)
    forAll(validEvents) { (event, expected) =>
      withClue("Event did not parse correctly") {
        eventParser.parseEvent(event) should ===(Right(expected))
      }
    }
  }

  private def expectedGameState(team1Score: Int, team2Score: Int, matchTime: Int): GameState =
    GameState(NonNegInt.unsafeFrom(team1Score), NonNegInt.unsafeFrom(team2Score), NonNegInt.unsafeFrom(matchTime))
}