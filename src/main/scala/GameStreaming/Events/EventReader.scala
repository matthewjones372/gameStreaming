package GameStreaming.Events
import java.io.{BufferedReader, FileReader}
import java.util.concurrent.ConcurrentLinkedQueue

import GameStreaming.Games.BasketBall.TeamScored
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Using
import scala.jdk.CollectionConverters._

trait EventReader[F[_]] {
  def addEvent(event: String): F[Either[GameEventError, Boolean]]
  def last: F[Option[_ <: GameEvent]]
  def lastN(n: Int): F[Seq[_ <: GameEvent]]
  def all: F[Seq[_ <: GameEvent]]
}

class FileEventReader(eventFilePath: String, eventParser: EventParser)(implicit ec: ExecutionContext)
    extends EventReader[Future]
    with StrictLogging {
  private val eventBuffer = new ConcurrentLinkedQueue[TeamScored]()

  def hydrateBuffer(): Unit =
    Using.resource(new BufferedReader(new FileReader(eventFilePath))) { reader =>
      Iterator.continually(reader.readLine()).takeWhile(_ != null).foreach { event =>
        eventParser.parseEvent(event) match {
          case Left(error: GameEventError) =>
            logger.error(error.msg)
          case Right(parsedEvent: TeamScored) =>
            eventBuffer.add(parsedEvent)
        }
      }
    }

  override def last: Future[Option[TeamScored]]        = Future(eventBuffer.asScala.lastOption)
  override def lastN(n: Int): Future[List[TeamScored]] = Future(eventBuffer.asScala.takeRight(n).toList)
  override def all: Future[List[TeamScored]]           = Future(eventBuffer.asScala.toList)

  override def addEvent(event: String): Future[Either[GameEventError, Boolean]] = Future{
    eventBuffer.forEach(println)
    eventParser.parseEvent(event) match {
      case Left(error) =>
        Left(error)
      case Right(parsedEvent: TeamScored) =>
        Right(eventBuffer.add(parsedEvent))
    }
  }
}
