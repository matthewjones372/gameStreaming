package Games.BasketBall.FormatSpecification
import Games.BasketBall.FormatSpecification.EventFormatSpecification.Offset

trait EventFormatSpecification {
  def bitLength: Int
  def MatchScore: Offset
  def Team1Score: Offset
  def Team2Score: Offset
  def ScoringTeam: Offset
  def MatchTime: Offset
}
object EventFormatSpecification {
  case class Offset(from: Int, to: Int)
}
