package lambda.stream

import cats.effect.{IO, IOApp, Ref, Resource}
import cats.effect.std.Random
import fs2.Stream
import scala.concurrent.duration._

/** Program 1: Infinite data. */
object Basics1_1 extends IOApp.Simple {
  override final val run: IO[Unit] =
    Stream(1, 2, 3)
      .covary[IO]
      .compile
      .toList
      .flatMap(IO.println)
}

object Basics1_2 extends IOApp.Simple {
  override final val run: IO[Unit] =
    Stream
      .iterate(0)(x => x + 1)
      .evalTap(IO.println)
      .take(10)
      .compile
      .toList
      .flatMap(IO.println)
}

object Basics1_3 extends IOApp.Simple {
  override final val run: IO[Unit] =
    Random.scalaUtilRandomSeedInt[IO](3).flatMap { random =>
      Stream
        .repeatEval(random.nextInt)
        .take(10)
        .compile
        .toList
        .flatMap(IO.println)
    }
}
// ----------------------------------------------


/** Program 2: Resource and effects management. */
object Basics2 extends IOApp.Simple {
  final class DBService {
    val streamAllData: Stream[IO, Int] =
      Stream.range(start = 0, stopExclusive = 21, step = 2)
  }
  object DBService {
    final val instance: Resource[IO, DBService] =
      Resource.make(
        IO.println("Connecting to the database").as(new DBService)
      ) { _ =>
        IO.println("Closing the database connection")
      }
  }

  override final val run: IO[Unit] =
    Stream.resource(DBService.instance).flatMap { db =>
      Stream.eval(IO.println("Before query the db")) ++
      db.streamAllData.evalMap(IO.println) ++
      Stream.eval(IO.println("After query the db"))
    }.compile.drain
}
// ----------------------------------------------


/** Program 3: Error handling. */
object Basics3 extends IOApp.Simple {
  final case object Failure extends Throwable("Failure")

  override final val run: IO[Unit] =
    Stream(1, 2, 3).evalMap {
      case 1 => IO.pure("Foo")
      case 2 => IO.raiseError(Failure)
      case _ => IO.raiseError(new Exception("Fatal"))
    }.handleErrorWith {
      case Failure => Stream("Bar", "Baz")
      case fatal => Stream.exec(IO.println(s"Fatal error ${fatal}"))
    }.evalMap(IO.println).compile.drain
}
// ----------------------------------------------


/** Program 4: Control flow & concurrency. */
object Basics4 extends IOApp.Simple {
  def greetStream(nameRef: Ref[IO, String]): Stream[IO, Nothing] =
    Stream.fixedDelay[IO](1.second).foreach { _ =>
      nameRef.get.flatMap { name =>
        IO.println(s"Hello ${name}!")
      }
    }

  def updatesStream(nameRef: Ref[IO, String]): Stream[IO, Unit] =
    Stream.repeatEval(IO.readLine).evalMap { line =>
      if (line.isBlank) IO.none[Unit]
      else nameRef.set(line.trim).option
    }.unNoneTerminate

  override final val run: IO[Unit] =
    IO.ref("Luis").flatMap { nameRef =>
      updatesStream(nameRef)
        .concurrently(greetStream(nameRef))
        .compile
        .drain
    }
}
// ----------------------------------------------
