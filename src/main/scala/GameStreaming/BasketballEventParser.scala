package GameStreaming

import GameStreaming.BasketballEvent.TeamScored
import GameStreaming.BasketballPoint.{OnePointer, ThreePointer, TwoPointer}
import GameStreaming.BasketballTeam.{Team1, Team2}
import GameStreaming.EventFormat.EventFormatSpecification
import GameStreaming.EventFormat.EventFormatSpecification.Offset
import eu.timepit.refined.types.numeric.NonNegInt

import scala.util.{Failure, Success, Try}

class BasketballEventParser(eventFormat: EventFormatSpecification) extends EventParser {
  import BasketballEventParser._

  override def parseEvent(event: String): Either[BasketballEventError, TeamScored] =
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
    } match {
      // This could be done simply with cats leftMap
      case Success(bits) => Right(bits)
      case Failure(_)    => Left(InvalidEventString(s"Could not parse input event: $event"))
    }
}
object BasketballEventParser {
  final case class InvalidEventString(msg: String) extends BasketballEventError
  final case class InvalidScore(msg: String) extends BasketballEventError
  final case class TeamScoreParseError(msg: String) extends BasketballEventError
  final case class GameTimeParseError(msg: String, error: String) extends BasketballEventError

  private def decodeSegment(eventVector: Seq[Char], offset: Offset): Int = {
    val segmentString = eventVector.slice(offset.from, offset.to).reverse.mkString
    Integer.valueOf(segmentString, 2)
  }

  private def decodeTeamScore(teamScore: Int): Either[TeamScoreParseError, NonNegInt] =
    NonNegInt.from(teamScore) match {
      case Left(_)      => Left(TeamScoreParseError("Team Scores must be Non-Negative"))
      case Right(value) => Right(value)
    }

  private def decodeGameTime(gameTime: Int): Either[GameTimeParseError, NonNegInt] =
    NonNegInt.from(gameTime) match {
      case Left(error)  => Left(GameTimeParseError("Game Time must be Non-Negative", error))
      case Right(value) => Right(value)
    }

  private def decodeScore(pointScore: Int): Either[InvalidScore, BasketballPoint] =
    pointScore match {
      case 1 => Right(OnePointer)
      case 2 => Right(TwoPointer)
      case 3 => Right(ThreePointer)
      case _ => Left(InvalidScore(s"Decoded points scored: $pointScore is not possible"))
    }

  private def decodeScoringTeam(scoringBit: Int): Either[BasketballEventError, BasketballTeam] =
    scoringBit match {
      case 0 => Right(Team1)
      case 1 => Right(Team2)
      case _ => Left(TeamScoreParseError(s"Scoring team: $scoringBit is not possible"))
    }
}
