package lambda.stream.data

import cats.effect.{IO, IOApp}
import cats.syntax.all.*
import fs2.io.file.{Files, Path}

object Main extends IOApp.Simple {
  def fahrenheitToCelsius(f: Double): Double =
    (f - 32.0) * (5.0 / 9.0)

  override final val run: IO[Unit] =
    Files[IO]
      .readAll(Path("fahrenheit.txt"))
      .through(fs2.text.utf8.decode)
      .through(fs2.text.lines)
      .tail
      .mapFilter(line => line.toDoubleOption)
      .map(fahrenheitToCelsius)
      .map(celsius => celsius.toString)
      .intersperse("\n")
      .through(fs2.text.utf8.encode)
      .through(Files[IO].writeAll(Path("celsius.txt")))
      .compile
      .drain
}
