package lambda.io.priority

import cats.effect.IO
import cats.effect.std.{Queue, Semaphore, Supervisor}
import scala.concurrent.duration.given // Provides duration units.

object HighLowPriorityRunner {
  final case class Config(
      highPriorityJobs: Queue[IO, IO[Unit]],
      lowPriorityJobs: Queue[IO, IO[Unit]],
      rateLimiter: Semaphore[IO]
  )

  def apply(config: Config): IO[Unit] =
    Supervisor[IO].use { supervisor =>
      val nextJob =
        config.highPriorityJobs.tryTake.flatMap {
          case Some(hpJob) => hpJob
          case None => config.lowPriorityJobs.tryTake.flatMap {
            case Some(lpJob) => lpJob
            case None => IO.sleep(100.millis)
          }
        }

      val processOneJob =
        config.rateLimiter.acquire >>
        supervisor.supervise(nextJob.guarantee(config.rateLimiter.release))

      processOneJob.foreverM.void
    }
}
