package GameStreaming.Utils
import GameStreaming.BasketballEvent.TeamScored
import GameStreaming.EventFormat.EventFormatV1
import GameStreaming.{BasketballEventParser, BasketballPoint, BasketballTeam, GameState}
import eu.timepit.refined.types.numeric.NonNegInt
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.magnolia._
import org.scalacheck.{Arbitrary, Gen}

object TeamScoreTesting {
  val eventParser = new BasketballEventParser(EventFormatV1)

  def expectedGameState(team1Score: Int, team2Score: Int, matchTime: Int): GameState =
    GameState(NonNegInt.unsafeFrom(team1Score), NonNegInt.unsafeFrom(team2Score), NonNegInt.unsafeFrom(matchTime))

  implicit val gameStateGen: Arbitrary[GameState] = Arbitrary(
    for {
      team1Score <- Gen.chooseNum(0, 127)
      team2Score <- Gen.chooseNum(0, 127)
      gameTime   <- Gen.chooseNum(0, 2047)
    } yield GameState(
      NonNegInt.unsafeFrom(team1Score),
      NonNegInt.unsafeFrom(team2Score),
      NonNegInt.unsafeFrom(gameTime)
    )
  )

  implicit val teamScoreGen: Gen[TeamScored] =
    for {
      pointsScore <- arbitrary[BasketballPoint]
      scoringTeam <- arbitrary[BasketballTeam]
      gameSate    <- arbitrary[GameState]
    } yield TeamScored(pointsScore, scoringTeam, gameSate)

  implicit class TeamScoredImplicits(teamScored: TeamScored) {
    def toHexString: String = {
      val pointsScored = teamScored.pointScored match {
        case BasketballPoint.OnePointer   => 1.toBinaryString.reverse.padTo(2, 0).reverse
        case BasketballPoint.TwoPointer   => 2.toBinaryString.reverse.padTo(2, 0).reverse
        case BasketballPoint.ThreePointer => 3.toBinaryString.reverse.padTo(2, 0).reverse
      }

      val scoringTeam = teamScored.scoringTeam match {
        case BasketballTeam.Team1 => 0.toBinaryString.toVector
        case BasketballTeam.Team2 => 1.toBinaryString.toVector
      }

      val team1Score = teamScored.gameState.team1Score.value.toBinaryString.reverse.padTo(8, 0).reverse
      val team2Score = teamScored.gameState.team2Score.value.toBinaryString.reverse.padTo(8, 0).reverse

      val gameTime = teamScored.gameState.matchTime.value.toBinaryString.reverse.padTo(12, 0).reverse
      val binaryString = Vector(Vector(0), gameTime, team1Score, team2Score, scoringTeam, pointsScored).flatten

      s"0x${Integer.toString(Integer.parseInt(binaryString.mkString, 2), 16)}"
    }
  }
}
