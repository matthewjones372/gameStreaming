package Games.BasketBall.EventFormat
import Games.BasketBall.EventFormat.EventFormatSpecification.Offset

object EventFormatV1 extends EventFormatSpecification {
  val BitLength           = 32
  val MatchScore: Offset  = Offset(0, 2)
  val Team1Score: Offset  = Offset(11, 18)
  val Team2Score: Offset  = Offset(3, 10)
  val ScoringTeam: Offset = Offset(2, 3)
  val MatchTime: Offset   = Offset(19, 31)
}
