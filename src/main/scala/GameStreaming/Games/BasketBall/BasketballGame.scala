package GameStreaming.Games.BasketBall

import GameStreaming.Events.{GameEvent, GameEventError}
import eu.timepit.refined.types.numeric.NonNegInt

sealed trait BasketballTeam

object BasketballTeam {
  case object Team1 extends BasketballTeam
  case object Team2 extends BasketballTeam
}

final case class GameState(team1Score: NonNegInt, team2Score: NonNegInt, matchTime: NonNegInt) {
  def isConsistentWith(that: GameState): Boolean = {
    that.matchTime.value > matchTime.value &&
    that.team1Score.value >= that.team1Score.value &&
    that.team2Score.value >= that.team2Score.value
  }
}

sealed trait BasketBallPoint

object BasketBallPoint {
  case object OnePointer extends BasketBallPoint
  case object TwoPointer extends BasketBallPoint
  case object ThreePointer extends BasketBallPoint
}

final case class TeamScored(
  pointScored: BasketBallPoint,
  scoringTeam: BasketballTeam,
  gameState: GameState
) extends GameEvent {
  def isConsistentWith(that: TeamScored): Boolean = {
    this.gameState.isConsistentWith(that.gameState)
  }
}
object TeamScored {
  case class NonConsistentState(msg: String) extends GameEventError
}
