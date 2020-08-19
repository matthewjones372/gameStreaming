package GameStreaming.HydrationSource
import java.io.{BufferedReader, FileReader}

import scala.util.Using

class FileHydrationSource(eventFilePath: String) extends HydrationSource[String] {
  override def readAll: List[String] =
    Using.resource(new BufferedReader(new FileReader(eventFilePath))) { reader =>
      Iterator.continually(reader.readLine()).takeWhile(_ != null).toList
    }
}
