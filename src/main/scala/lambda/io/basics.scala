package lambda.io

import cats.effect.{IO, IOApp, Resource}
import cats.syntax.all._
import scala.concurrent.duration._ // Provides duration units.
import scala.io.Source

/** Program 1: Sequential composition. */
object Basics1_1 extends IOApp.Simple {
  val myIO: IO[Unit] =
    IO.println("Hello, World!")

  override final val run: IO[Unit] =
    myIO.flatMap(_ => myIO)
}

object Basics1_2 extends IOApp.Simple {
  val myIO: IO[Unit] =
    IO.println("Hello, World!")

  override final val run: IO[Unit] =
    myIO >> myIO >> myIO >> myIO >> myIO
}
// ----------------------------------------------


/** Program 2: Async & Cancellable operations. */
object Basics2_1 extends IOApp.Simple {
  val tick: IO[Unit] =
    (IO.sleep(1.second) >> IO.println("Tick")).foreverM

  val cancelToken: IO[Unit] =
    IO.readLine.void

  override final val run: IO[Unit] =
    tick.start.flatMap { tickFiber =>
      cancelToken >> tickFiber.cancel
    }
}

object Basics2_2 extends IOApp.Simple {
  val tick: IO[Unit] =
    (IO.sleep(1.second) >> IO.println("Tick")).foreverM

  val cancelToken: IO[Unit] =
    IO.readLine.void

  override final val run: IO[Unit] =
    tick.background.surround(cancelToken)
}
// ----------------------------------------------


/** Program 3: Parallel operations. */
object Basics3_1 extends IOApp.Simple {
  val ioA: IO[Int] =
    IO.sleep(1.second) >> IO.println("Running ioA").as(1)

  val ioB: IO[String] =
    IO.sleep(1.second) >> IO.println("Running ioB").as("Balmung")

  val ioC: IO[Boolean] =
    IO.sleep(1.second) >> IO.println("Running ioC").as(true)

  override final val run: IO[Unit] =
    (ioA, ioB, ioC).parTupled.flatMap {
      case (a, b, c) =>
        IO.println(s"a: ${a} | b: ${b} | c: ${c}")
    }
}

object Basics3_2 extends IOApp.Simple {
  override final val run: IO[Unit] =
    List.range(start = 0, end = 11).parTraverse { i =>
      IO.sleep(1.second) >> IO.println(i) >> IO.delay((i * 10) + 5)
    } flatMap { data =>
      IO.println(s"Final data: ${data}")
    }
}
// ----------------------------------------------


/** Program 4: Error handling and resource management. */
object Basics4 extends IOApp.Simple {
  def fahrenheitToCelsius(f: Double): Double =
    (f - 32.0) * (5.0 / 9.0)

  def process(lines: List[String]): List[Double] =
    lines.filter { line =>
      !line.isBlank && !line.startsWith("//")
    } map { line =>
      fahrenheitToCelsius(f = line.toDouble)
    }

  override final val run: IO[Unit] =
    Resource
      .fromAutoCloseable(IO(Source.fromFile("fahrenheit.txt")))
      .use(file => IO(file.getLines().toList))
      .map(process)
      .attempt
      .flatMap {
        case Right(data) =>
          IO.println(s"Output: ${data.take(5).mkString("[", ", ", ", ...]")}")

        case Left(ex) =>
          IO.println(s"Error: ${ex.getMessage}")
      }
}
// ----------------------------------------------
