package Games.BasketBall
import Games.BasketBall.BasketBallPoint.{OnePointer, ThreePointer, TwoPointer}
import Games.BasketBall.BasketballTeam.{Team1, Team2}
import Games.BasketBall.FormatSpecification.EventFormatSpecification
import Games.BasketBall.FormatSpecification.EventFormatSpecification.Offset
import eu.timepit.refined.types.numeric.NonNegInt
import cats.implicits._

import scala.util.Try

class BasketBallEventParser(eventFormat: EventFormatSpecification) extends EventParser {
  import BasketBallEventParser._

  override def parseEvent(event: String): Either[GameEventError, GameEvent] = {
    for {
      bitVector  <- convertToBinaryList(event)
      score      <- decodeScore(decodeSegment(bitVector, eventFormat.MatchScore))
      teamScored <- decodeScoringTeam(decodeSegment(bitVector, eventFormat.ScoringTeam))
      team1score <- decodeTeamScore(decodeSegment(bitVector, eventFormat.Team1Score))
      team2Score <- decodeTeamScore(decodeSegment(bitVector, eventFormat.Team2Score))
      matchTime  <- decodeGameTime(decodeSegment(bitVector, eventFormat.MatchTime))
    } yield TeamScored(score, teamScored, GameState(team1score, team2Score, matchTime))
  }

  private def convertToBinaryList(event: String): Either[LineParseError, List[Char]] = {
    Try {
      Integer
        .decode(event)
        .toInt
        .toBinaryString
        .reverse
        .padTo(eventFormat.bitLength, 0)
        .mkString
        .toList
    }.toEither.leftMap(error => LineParseError(error))
  }
}
object BasketBallEventParser {
  final case class LineParseError(exception: Throwable) extends GameEventError
  final case class GameScoreParseError(message: String) extends GameEventError
  final case class TeamScoreParseError(message: String, error: String) extends GameEventError
  final case class GameTimeParseError(message: String, error: String) extends GameEventError

  private def decodeSegment(eventVector: Seq[Char], offset: Offset): Int = {
    val segmentString = eventVector.slice(offset.from, offset.to).reverse.mkString
    Integer.valueOf(segmentString, 2)
  }

  private def decodeTeamScore(teamScore: Int): Either[TeamScoreParseError, NonNegInt] = {
    NonNegInt.from(teamScore).leftMap(error => TeamScoreParseError("Team Scores must be Non-Negative", error))
  }

  private def decodeGameTime(gameTime: Int): Either[GameTimeParseError, NonNegInt] = {
    NonNegInt.from(gameTime).leftMap(error => GameTimeParseError("Games.BasketBall.Game time must be Non-Negative", error))
  }

  private def decodeScore(pointScore: Int): Either[GameScoreParseError, BasketBallPoint] = {
    pointScore match {
      case 1 => Right(OnePointer)
      case 2 => Right(TwoPointer)
      case 3 => Right(ThreePointer)
      case _ => Left(GameScoreParseError(s"Decoded points scored: ${pointScore} is not valid"))
    }
  }

  private def decodeScoringTeam(scoringBit: Int): Either[GameEventError, BasketballTeam] = {
    Right(scoringBit match {
      case 0 => Team1
      case 1 => Team2
    })
  }
}
