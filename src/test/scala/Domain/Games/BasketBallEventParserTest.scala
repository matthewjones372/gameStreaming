package Domain.Games

import Games.BasketBall.BasketBallEventParser.{InvalidEventString, InvalidScore}
import Games.BasketBall.BasketBallPoint._
import Games.BasketBall.BasketballTeam._
import Games.BasketBall.EventFormat.EventFormatV1
import Games.BasketBall._
import eu.timepit.refined.types.numeric.NonNegInt
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.magnolia.gen
import org.scalacheck.{Arbitrary, Gen}
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatest.prop.TableFor2
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class BasketBallEventParserTest extends AnyWordSpec with Matchers with TypeCheckedTripleEquals with TeamScoreTesting {
  "BasketBallEventParser" should {
    "parse valid event examples" in {
      val validEventExamples: TableFor2[String, TeamScored] = Table(
        ("Event Hex", "Expected Event Decoded"),
        ("0x781002", TeamScored(TwoPointer, Team1, expectedGameState(2, 0, 15))),
        ("0xf0101f", TeamScored(ThreePointer, Team2, expectedGameState(2, 3, 30))),
        ("0x1310c8a1", TeamScored(OnePointer, Team1, expectedGameState(25, 20, 610))),
        ("0x29f981a2", TeamScored(TwoPointer, Team1, expectedGameState(48, 52, 1343))),
        ("0x48332327", TeamScored(ThreePointer, Team2, expectedGameState(100, 100, 2310)))
      )

      forAll(validEventExamples) {
        case (event, expected) =>
          withClue("Event did not parse correctly") {
            eventParser.parseEvent(event) should ===(Right(expected))
          }
      }
    }

   "parse valid events with values in the minimum and maximum range" in {
      ScalaCheckDrivenPropertyChecks.forAll(teamScoreArb) { event: TeamScored =>
        withClue("Could not parse event with valid values") {
          eventParser.parseEvent(event.toHex) should ===(Right(event))
        }
      }
    }

    "return an InvalidEventString error when given an invalid event string" in {
      eventParser.parseEvent("") should ===(Left(InvalidEventString("Could not parse input event: ")))
      eventParser.parseEvent("SOME_INVALID_STRING") should ===(Left(InvalidEventString("Could not parse input event: SOME_INVALID_STRING")))
    }

    "return an GameScoreParseError when given an invalid score event" in {
      //An invalid event with a score of 0
      val invalidEvent = "0x781000"
      eventParser.parseEvent(invalidEvent) should ===(Left(InvalidScore("Decoded points scored: 0 is not valid")))
    }
  }
}

trait TeamScoreTesting {
  val eventParser = new BasketBallEventParser(EventFormatV1)

  def expectedGameState(team1Score: Int, team2Score: Int, matchTime: Int): GameState =
    GameState(NonNegInt.unsafeFrom(team1Score), NonNegInt.unsafeFrom(team2Score), NonNegInt.unsafeFrom(matchTime))

  implicit val gameStateGen: Arbitrary[GameState] = Arbitrary(
    for {
      team1Score <- Gen.choose(0, 128)
      team2Score <- Gen.choose(0, 128)
      gameTime   <- Gen.choose(0, 2048)
    } yield GameState(
      NonNegInt.unsafeFrom(team1Score),
      NonNegInt.unsafeFrom(team2Score),
      NonNegInt.unsafeFrom(gameTime)
    )
  )

  implicit val teamScoreArb: Gen[TeamScored] =
    for {
      pointsScore <- arbitrary[BasketBallPoint]
      scoringTeam <- arbitrary[BasketballTeam]
      gameSate    <- arbitrary[GameState]
    } yield TeamScored(pointsScore, scoringTeam, gameSate)

  implicit class TeamScoreExtras(teamScored: TeamScored) {
    def toHex: String = {
      val pointsScored = teamScored.pointScored match {
        case BasketBallPoint.OnePointer   => 1.toBinaryString.reverse.padTo(2, 0).reverse
        case BasketBallPoint.TwoPointer   => 2.toBinaryString.reverse.padTo(2, 0).reverse
        case BasketBallPoint.ThreePointer => 3.toBinaryString.reverse.padTo(2, 0).reverse
      }

      val scoringTeam = teamScored.scoringTeam match {
        case BasketballTeam.Team1 => 0.toBinaryString.toVector
        case BasketballTeam.Team2 => 1.toBinaryString.toVector
      }

      val team1Score = teamScored.gameState.team1Score.value.toBinaryString.reverse.padTo(8, 0).reverse
      val team2Score = teamScored.gameState.team2Score.value.toBinaryString.reverse.padTo(8, 0).reverse

      val gameTime     = teamScored.gameState.matchTime.value.toBinaryString.reverse.padTo(12, 0).reverse
      val binaryString = Vector(Vector(0), gameTime, team1Score, team2Score, scoringTeam, pointsScored).flatten

      s"0x${Integer.toString(Integer.parseInt(binaryString.mkString, 2), 16)}"
    }
  }
}
