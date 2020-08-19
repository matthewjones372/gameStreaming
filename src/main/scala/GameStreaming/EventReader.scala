package GameStreaming

import GameStreaming.BasketballEvent.TeamScored
import GameStreaming.HydrationSource.HydrationSource
import GameStreaming.Identity.Id
import com.typesafe.scalalogging.StrictLogging

import scala.collection.mutable

object Identity {
  type Id[A] = A
}

trait EventReader[F[_]] {
  case class NonConsistentEvent(msg: String) extends BaskBallEventError

  def add(event: String): F[Either[BaskBallEventError, Boolean]]
  def hydrateBuffer(): F[Unit]
  def last: F[Option[BasketballEvent]]
  def lastN(n: Int): F[Seq[BasketballEvent]]
  def all: F[Seq[BasketballEvent]]
}

class IdEventReader(hydrationSource: HydrationSource[String], eventParser: EventParser)
    extends EventReader[Id]
    with StrictLogging {

  private val eventBuffer = new mutable.ListBuffer[BasketballEvent]()

  override def hydrateBuffer(): Unit =
    hydrationSource.readAll.foreach(event => add(event))

  override def last: Option[BasketballEvent] =
    eventBuffer.lastOption

  override def lastN(n: Int): List[BasketballEvent] =
    eventBuffer.takeRight(n).toList

  override def all: List[BasketballEvent] =
    eventBuffer.toList

  override def add(event: String): Either[BaskBallEventError, Boolean] =
    eventParser.parseEvent(event) match {
      case Left(error: BaskBallEventError) =>
        logger.error(s"Error adding event: $event returned an error ${error.msg}")
        Left(error)
      case Right(newEvent: TeamScored) =>
        val lastEvent = this.last
        if (newEvent.isConsistentWith(lastEvent)) {
          eventBuffer.addOne(newEvent)
          logger.info(s"Event: $event added to Event Buffer")
          Right(true) // This should be more robust
        } else {
          val error = NonConsistentEvent(
            s"$newEvent is state is not consistent with last event ${lastEvent.get} and will be discarded"
          )
          logger.error(error.msg)
          Left(error)
        }
    }
}
