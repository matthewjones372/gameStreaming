package GameStreaming.Events.HydrationSource

trait HydrationSource[F] {
  def readAll: Seq[F]
}
