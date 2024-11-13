package GameStreaming

import GameStreaming.BasketballEvent.TeamScored
import GameStreaming.BasketballPoint.{ OnePointer, ThreePointer, TwoPointer }
import GameStreaming.BasketballTeam.{ Team1, Team2 }
import GameStreaming.EventFormat.EventFormatSpecification
import GameStreaming.EventFormat.EventFormatSpecification.Offset
import eu.timepit.refined.types.numeric.NonNegInt
import scala.util.{ Failure, Success, Try }

class BasketballEventParser(eventFormat: EventFormatSpecification) extends EventParser {
  import BasketballEventParser._

  override def parseEvent(event: String): Either[BasketballEventError, TeamScored] =
    convertToInt(event).flatMap { bitVector =>
      for {
        score      <- decodeScore(getDecodedSegment(bitVector, eventFormat.MatchScore))
        teamScored <- decodeScoringTeam(getDecodedSegment(bitVector, eventFormat.ScoringTeam))
        team1score <- decodeTeamScore(getDecodedSegment(bitVector, eventFormat.Team1Score))
        team2Score <- decodeTeamScore(getDecodedSegment(bitVector, eventFormat.Team2Score))
        matchTime  <- decodeGameTime(getDecodedSegment(bitVector, eventFormat.MatchTime))
      } yield TeamScored(score, teamScored, GameState(team1score, team2Score, matchTime))
    }

  private def convertToInt(event: String): Either[InvalidEventString, Int] = {
    Try(Integer.decode(event).toInt) match {
      case Success(value) => Right(value)
      case Failure(_)     => Left(InvalidEventString(s"Could not parse input event: $event"))
    }
  }

  private def getDecodedSegment(bitVector: Int, offset: Offset): Int = {
    val mask = (1 << (offset.to - offset.from)) - 1
    (bitVector >> offset.from) & mask
  }

  private def decodeTeamScore(teamScore: Int): Either[TeamScoreParseError, NonNegInt] =
    NonNegInt.from(teamScore).left.map(_ => TeamScoreParseError("Team Scores must be Non-Negative"))

  private def decodeGameTime(gameTime: Int): Either[GameTimeParseError, NonNegInt] =
    NonNegInt.from(gameTime).left.map(error => GameTimeParseError("Game Time must be Non-Negative", error))

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

object BasketballEventParser {
  final case class InvalidEventString(msg: String)                          extends BasketballEventError
  final case class InvalidScore(msg: String)                                extends BasketballEventError
  private final case class TeamScoreParseError(msg: String)                 extends BasketballEventError
  private final case class GameTimeParseError(msg: String, error: String)() extends BasketballEventError
}
