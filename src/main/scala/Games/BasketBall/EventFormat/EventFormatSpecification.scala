package Games.BasketBall.EventFormat
import Games.BasketBall.EventFormat.EventFormatSpecification.Offset

trait EventFormatSpecification {
  def BitLength: Int
  def MatchScore: Offset
  def Team1Score: Offset
  def Team2Score: Offset
  def ScoringTeam: Offset
  def MatchTime: Offset
}
object EventFormatSpecification {
  case class Offset(from: Int, to: Int)
}
