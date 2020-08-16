package GameStreaming.Games.BasketBall.EventFormat

import GameStreaming.Games.BasketBall.EventFormat.EventFormatSpecification.Offset

trait EventFormatSpecification {
  def BitLength: Int
  def MatchScore: Offset
  def Team1Score: Offset
  def Team2Score: Offset
  def ScoringTeam: Offset
  def MatchTime: Offset
}
object EventFormatSpecification {
  final case class Offset(from: Int, to: Int)
}
