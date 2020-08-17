package GameStreaming.Events
import GameStreaming.Games.{ GameEvent, GameEventError }

trait EventParser {
  def parseEvent(event: String): Either[GameEventError, GameEvent]
}
