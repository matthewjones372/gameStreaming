package GameStreaming.Events

trait EventParser {
  def parseEvent(event: String): Either[GameEventError, GameEvent]
}
