package lambda.io.priority

import cats.effect.{IO, IOApp}
import cats.effect.std.{Queue, Semaphore}
import cats.syntax.all.*
import scala.concurrent.duration.given // Provides duration units.

object Main extends IOApp.Simple {
  private def createJob(id: Int, highPriority: Boolean = true): IO[Unit] =
    IO.println(s"Starting (${if (highPriority) "High" else "Low"} priority) job ${id}") >>
    IO.sleep(1.second) >>
    IO.println(s"Finished job ${id}!")

  override final val run: IO[Unit] =
    (
      Queue.unbounded[IO, IO[Unit]],
      Queue.unbounded[IO, IO[Unit]],
      Semaphore[IO](n = 2)
    ).mapN(HighLowPriorityRunner.Config.apply).flatMap { config =>
      HighLowPriorityRunner(config).background.surround {
        List.range(0, 10).traverse_(id => config.lowPriorityJobs.offer(createJob(id, highPriority = false))) >>
        IO.sleep(100.millis) >>
        List.range(10, 15).traverse_(id => config.highPriorityJobs.offer(createJob(id))) >>
        IO.sleep(4.seconds) >>
        List.range(15, 20).traverse_(id => config.highPriorityJobs.offer(createJob(id))) >>
        IO.sleep(4.seconds)
      }
    }
}
