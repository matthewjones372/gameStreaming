package GameStreaming.Events
import java.io.{BufferedReader, FileReader}
import java.util.concurrent.ConcurrentLinkedDeque

import GameStreaming.Games.BasketBall.TeamScored
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
import scala.util.Using

trait EventReader[F[_]] {
  case class NonConsistentEvent(msg: String) extends GameEventError
  def add(event: String): F[Either[GameEventError, Boolean]]
  def last: F[Option[_ <: GameEvent]]
  def lastN(n: Int): F[Seq[_ <: GameEvent]]
  def all: F[Seq[_ <: GameEvent]]
}

class FutureEventReader(eventFilePath: String, eventParser: EventParser)(implicit ec: ExecutionContext)
    extends EventReader[Future]
    with StrictLogging {
  private val eventBuffer = new ConcurrentLinkedDeque[TeamScored]()

  def hydrateBuffer(): Unit =
    Using.resource(new BufferedReader(new FileReader(eventFilePath))) { reader =>
      Iterator.continually(reader.readLine()).takeWhile(_ != null).foreach { event =>
        add(event)
      }
    }

  override def last: Future[Option[TeamScored]] =
    Future {
      if (eventBuffer.isEmpty) None
      else Some(eventBuffer.getLast)
    }

  override def lastN(n: Int): Future[List[TeamScored]] =
    Future(eventBuffer.asScala.takeRight(n).toList)

  override def all: Future[List[TeamScored]] =
    Future(eventBuffer.asScala.toList)

  override def add(event: String): Future[Either[GameEventError, Boolean]] =
    eventParser.parseEvent(event) match {
      case Left(error) =>
        Future(Left(error))
      case Right(newEvent: TeamScored) =>
        last.flatMap { lastEvent =>
          if (newEvent.isConsistentWith(lastEvent))
            Future(Right(eventBuffer.add(newEvent)))
          else
            Future(Left(NonConsistentEvent("")))
        }
    }
}
