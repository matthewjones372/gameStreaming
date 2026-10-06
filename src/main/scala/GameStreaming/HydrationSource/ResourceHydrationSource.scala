package GameStreaming.HydrationSource
import java.io.{BufferedReader, InputStreamReader}
import java.nio.charset.StandardCharsets

import scala.util.Using

class ResourceHydrationSource(resourceName: String) extends HydrationSource[String] {
  override def readAll: List[String] = {
    val stream = Option(getClass.getResourceAsStream(resourceName))
      .getOrElse(throw new IllegalArgumentException(s"Resource not found: $resourceName"))
    Using.resource(new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) { reader =>
      Iterator.continually(reader.readLine()).takeWhile(_ != null).toList
    }
  }
}
