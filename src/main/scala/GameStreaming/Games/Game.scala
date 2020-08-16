package GameStreaming.Games

import GameStreaming.Events.{ GameEvent, GameEventError }

trait Game[F[_]] {
  def lastEvent: F[Either[GameEventError, GameEvent]]
  def lastEvents(n: Int): F[Seq[Either[GameEventError, GameEvent]]]
  def allEvents: F[Seq[Either[GameEventError, GameEvent]]]
}
