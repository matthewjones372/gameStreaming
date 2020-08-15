package Games.BasketBall

import eu.timepit.refined.types.numeric.NonNegInt

sealed trait BasketballTeam

object BasketballTeam {
  case object Team1 extends BasketballTeam
  case object Team2 extends BasketballTeam
}

case class GameState(team1Score: NonNegInt, team2Score: NonNegInt, matchTime: NonNegInt)

sealed trait BasketBallPoint

object BasketBallPoint {
  case object OnePointer extends BasketBallPoint
  case object TwoPointer extends BasketBallPoint
  case object ThreePointer extends BasketBallPoint
}

case class TeamScored(
  pointScored: BasketBallPoint,
  scoringTeam: BasketballTeam,
  gameState: GameState
) extends GameEvent

class BasketballGame[F[_]](gameEventSource: EventReader[F]) extends Game[F] {
  override def lastEvent: F[Either[GameEventError, GameEvent]] =
    gameEventSource.last

  override def lastEvents(n: Int): F[Seq[Either[GameEventError, GameEvent]]] =
    gameEventSource.lastN(n)

  override def allEvents: F[Seq[Either[GameEventError, GameEvent]]] =
    gameEventSource.all
}
