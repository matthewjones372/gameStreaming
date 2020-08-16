package GameStreaming.Games.BasketBall

import GameStreaming.Events.{ EventParser, GameEvent, GameEventError }
import GameStreaming.Games.BasketBall.BasketBallPoint.{ OnePointer, ThreePointer, TwoPointer }
import GameStreaming.Games.BasketBall.BasketballTeam.{ Team1, Team2 }
import GameStreaming.Games.BasketBall.EventFormat.EventFormatSpecification
import GameStreaming.Games.BasketBall.EventFormat.EventFormatSpecification.Offset
import cats.implicits._
import eu.timepit.refined.types.numeric.NonNegInt

import scala.util.Try

class BasketBallEventParser(eventFormat: EventFormatSpecification) extends EventParser {
  import BasketBallEventParser._

  override def parseEvent(event: String): Either[GameEventError, TeamScored] =
    for {
      bitVector  <- convertToBinaryList(event)
      score      <- decodeScore(decodeSegment(bitVector, eventFormat.MatchScore))
      teamScored <- decodeScoringTeam(decodeSegment(bitVector, eventFormat.ScoringTeam))
      team1score <- decodeTeamScore(decodeSegment(bitVector, eventFormat.Team1Score))
      team2Score <- decodeTeamScore(decodeSegment(bitVector, eventFormat.Team2Score))
      matchTime  <- decodeGameTime(decodeSegment(bitVector, eventFormat.MatchTime))
    } yield TeamScored(score, teamScored, GameState(team1score, team2Score, matchTime))

  private def convertToBinaryList(event: String): Either[InvalidEventString, List[Char]] =
    Try {
      Integer
        .decode(event)
        .toInt
        .toBinaryString
        .reverse
        .padTo(eventFormat.BitLength, 0)
        .mkString
        .toList
    }.toEither
      .leftMap(_ => InvalidEventString(s"Could not parse input event: $event"))
}
object BasketBallEventParser {
  final case class InvalidEventString(msg: String)                 extends GameEventError
  final case class InvalidScore(msg: String)                       extends GameEventError
  final case class TeamScoreParseError(msg: String, error: String) extends GameEventError
  final case class GameTimeParseError(msg: String, error: String)  extends GameEventError

  private def decodeSegment(eventVector: Seq[Char], offset: Offset): Int = {
    val segmentString = eventVector.slice(offset.from, offset.to).reverse.mkString
    Integer.valueOf(segmentString, 2)
  }

  private def decodeTeamScore(teamScore: Int): Either[TeamScoreParseError, NonNegInt] =
    NonNegInt.from(teamScore).leftMap(error => TeamScoreParseError("Team Scores must be Non-Negative", error))

  private def decodeGameTime(gameTime: Int): Either[GameTimeParseError, NonNegInt] =
    NonNegInt.from(gameTime).leftMap(error => GameTimeParseError("Game Time must be Non-Negative", error))

  private def decodeScore(pointScore: Int): Either[InvalidScore, BasketBallPoint] =
    pointScore match {
      case 1 => Right(OnePointer)
      case 2 => Right(TwoPointer)
      case 3 => Right(ThreePointer)
      case _ => Left(InvalidScore(s"Decoded points scored: $pointScore is not possible"))
    }

  private def decodeScoringTeam(scoringBit: Int): Either[GameEventError, BasketballTeam] =
    Right(scoringBit match {
      case 0 => Team1
      case 1 => Team2
    })
}
