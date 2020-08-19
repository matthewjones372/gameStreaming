package GameStreaming.Events
import GameStreaming.Games.BasketBall.BasketballEvent

trait BaskBallEventError {
  def msg: String
}

trait EventParser {
  def parseEvent(event: String): Either[BaskBallEventError, BasketballEvent]
}
