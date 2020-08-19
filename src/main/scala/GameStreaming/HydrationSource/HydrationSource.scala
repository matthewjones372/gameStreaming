package GameStreaming.HydrationSource

trait HydrationSource[F] {
  def readAll: Seq[F]
}
