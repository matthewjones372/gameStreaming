package Games.BasketBall

import Games.BasketBall.Identity.Id

object Identity {
  type Id[A] = A
}

trait EventReader[F[_]] {
  def last: F[Either[GameEventError, GameEvent]]
  def lastN(n: Int): F[Seq[Either[GameEventError, GameEvent]]]
  def all: F[Seq[Either[GameEventError, GameEvent]]]
}

class FileEventReader(eventFilePath: String, eventParser: EventParser) extends EventReader[Id] {
  case object CannotReadFile extends GameEventError

//  private val events: Seq[String] = Using.resource(new BufferedReader(new FileReader(eventFilePath))) { reader =>
//    Iterator.continually(reader.readLine()).takeWhile(_ != null).toSeq
//  }
  override def last: Id[Either[GameEventError, GameEvent]]               = Left(CannotReadFile)
  override def lastN(n: Int): Id[Seq[Either[GameEventError, GameEvent]]] = List(Left(CannotReadFile))
  override def all: Id[Seq[Either[GameEventError, GameEvent]]]           = List(Left(CannotReadFile))
}
