package GameStreaming

import eu.timepit.refined.types.numeric.NonNegInt

sealed trait BasketballTeam

object BasketballTeam {
  case object Team1 extends BasketballTeam
  case object Team2 extends BasketballTeam
}

final case class GameState(team1Score: NonNegInt, team2Score: NonNegInt, matchTime: NonNegInt) {
  def isConsistentWith(lastGameState: GameState): Boolean =
    this.matchTime.value > lastGameState.matchTime.value &&
      this.team1Score.value >= lastGameState.team1Score.value &&
      this.team2Score.value >= lastGameState.team2Score.value
}

sealed trait BasketBallPoint

object BasketBallPoint {
  case object OnePointer   extends BasketBallPoint
  case object TwoPointer   extends BasketBallPoint
  case object ThreePointer extends BasketBallPoint
}

sealed trait BasketballEvent {
  def gameState: GameState
  def isConsistentWith(lastEvent: Option[BasketballEvent]): Boolean
}

object BasketballEvent {
  case class TeamScored(
      pointScored: BasketBallPoint,
      scoringTeam: BasketballTeam,
      gameState: GameState
  ) extends BasketballEvent {
    def isConsistentWith(lastTeamScoredMaybe: Option[BasketballEvent]): Boolean =
      lastTeamScoredMaybe.forall { lastTeamScore =>
        this.gameState.isConsistentWith(lastTeamScore.gameState)
      }
  }
}
