package GameStreaming.Games.BasketBall
import GameStreaming.Games.BasketBall.BasketballEvent.TeamScored
import GameStreaming.Utils.TeamScoreTesting._
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TeamScoredTest extends AnyWordSpec with TypeCheckedTripleEquals with Matchers {

  "TeamScored" should {
    "flag new event as consistent when team1's  score is increased" in {
      val firstEvent  = TeamScored(BasketBallPoint.OnePointer, BasketballTeam.Team1, expectedGameState(1, 2, 100))
      val secondEvent = TeamScored(BasketBallPoint.OnePointer, BasketballTeam.Team1, expectedGameState(2, 2, 105))
      shouldBeConsistent(firstEvent = firstEvent, secondEvent = secondEvent)
    }

    "flag new event as consistent when team2's score is increased" in {
      val firstEvent  = TeamScored(BasketBallPoint.OnePointer, BasketballTeam.Team1, expectedGameState(1, 2, 100))
      val secondEvent = TeamScored(BasketBallPoint.OnePointer, BasketballTeam.Team2, expectedGameState(1, 3, 105))
      shouldBeConsistent(firstEvent = firstEvent, secondEvent = secondEvent)
    }

    "flag new event as consistent when the game time is increased" in {
      val firstEvent  = TeamScored(BasketBallPoint.OnePointer, BasketballTeam.Team1, expectedGameState(1, 3, 100))
      val secondEvent = TeamScored(BasketBallPoint.OnePointer, BasketballTeam.Team2, expectedGameState(1, 4, 105))
      shouldBeConsistent(firstEvent = firstEvent, secondEvent = secondEvent)
    }

    "flag new event as inconsistent when team1's score is decreased" in {
      val firstEvent  = TeamScored(BasketBallPoint.OnePointer, BasketballTeam.Team1, expectedGameState(1, 2, 100))
      val secondEvent = TeamScored(BasketBallPoint.OnePointer, BasketballTeam.Team1, expectedGameState(0, 2, 105))
      shouldBeInconsistent(firstEvent = firstEvent, secondEvent = secondEvent)
    }

    "flag new event as inconsistent when team2's score is decreased" in {
      val firstEvent  = TeamScored(BasketBallPoint.OnePointer, BasketballTeam.Team1, expectedGameState(2, 2, 100))
      val secondEvent = TeamScored(BasketBallPoint.OnePointer, BasketballTeam.Team1, expectedGameState(0, 2, 105))
      shouldBeInconsistent(firstEvent = firstEvent, secondEvent = secondEvent)
    }

    "flag new event as inconsistent when game time is decreased" in {
      val firstEvent  = TeamScored(BasketBallPoint.OnePointer, BasketballTeam.Team1, expectedGameState(2, 2, 100))
      val secondEvent = TeamScored(BasketBallPoint.OnePointer, BasketballTeam.Team1, expectedGameState(0, 2, 105))
      shouldBeInconsistent(firstEvent = firstEvent, secondEvent = secondEvent)
    }

    def shouldBeConsistent(firstEvent: TeamScored, secondEvent: TeamScored): Assertion =
      secondEvent.isConsistentWith(Some(firstEvent)) should ===(true)

    def shouldBeInconsistent(firstEvent: TeamScored, secondEvent: TeamScored): Assertion =
      secondEvent.isConsistentWith(Some(firstEvent)) should ===(false)
  }
}
