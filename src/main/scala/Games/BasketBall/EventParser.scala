package Games.BasketBall

trait EventParser {
  def parseEvent(event: String): Either[GameEventError, GameEvent]
}
