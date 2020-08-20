package GameStreaming

trait BasketballEventError {
  def msg: String
}

trait EventParser {
  def parseEvent(event: String): Either[BasketballEventError, BasketballEvent]
}
