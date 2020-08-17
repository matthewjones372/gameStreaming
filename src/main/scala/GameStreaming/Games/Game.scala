package GameStreaming.Games

trait Game[F[_]] {
  def lastEvent: F[Either[GameEventError, GameEvent]]
  def lastEvents(n: Int): F[Seq[Either[GameEventError, GameEvent]]]
  def allEvents: F[Seq[Either[GameEventError, GameEvent]]]
}
