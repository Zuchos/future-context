package futures.context

import org.scalatest.FlatSpec

import java.util.concurrent.Executors
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration._

class Futures_0 extends FlatSpec {

  it should "run jobs on single thread" in {
    val ec = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
    val futures = (1 to 5)
      .map { jobId =>
        TestService.threadSleep(jobId, 3)(ec)
      }
    futures.foreach(f => Await.ready(f, Duration.Inf))
  }

  it should "run jobs on 10 threads" in {
    val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))
    val futures = (1 to 20)
      .map { jobId =>
        TestService
          .threadSleep(jobId, 3)(ec)
      }
    futures.foreach(f => Await.ready(f, Duration.Inf))
  }

  it should "run jobs on ?? threads" in {
    val ec = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
    val futures = (1 to 20)
      .map { jobId =>
        TestService
          .threadSleep(jobId, 3)(ec)
      }
    futures.foreach(f => Await.ready(f, Duration.Inf))
  }
}
