package GameStreaming

trait BaskBallEventError {
  def msg: String
}

trait EventParser {
  def parseEvent(event: String): Either[BaskBallEventError, BasketballEvent]
}
