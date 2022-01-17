package futures.context

import futures.context.TestConstants._
import org.scalatest.FlatSpec

import java.util.concurrent.Executors
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration._

class Futures_1 extends FlatSpec {

  it should "run cpu intensive futures on fixed thread pool" in {
    val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(6))
    val futures = (1 to 18)
      .map { jobId =>
        TestService
          .calculatePrimes(jobId, _30Sec)(ec)
      }
    futures.foreach(f => Await.ready(f, Duration.Inf))
  }

  it should "run cpu intensive futures on global thread pool" in {
    val futures = (1 to 18)
      .map { jobId =>
        TestService
          .calculatePrimes(jobId, _10Sec)(global)
      }
    futures.foreach(f => Await.ready(f, Duration.Inf))
  }

  it should "run blocking sleeping futures on global thread pool" in {
    val futures = (1 to 24)
      .map { jobId =>
        TestService
          .threadSleepBlocking(jobId, 5)(global)
      }

    futures.foreach(f => Await.ready(f, Duration.Inf))
  }

}
