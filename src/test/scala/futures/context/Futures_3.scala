package futures.context

import futures.context.TestConstants._10Sec
import org.scalatest.FlatSpec

import java.util.concurrent.{
  ArrayBlockingQueue,
  Executors,
  LinkedBlockingQueue,
  SynchronousQueue,
  ThreadPoolExecutor,
  TimeUnit
}
import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class Futures_3 extends FlatSpec {

  it should "run cpu intensive blocking features along with sleeping futures on dedicated thread pools" in {
    val blockingContext =
      ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
    val futures1 = (1 to 3).map { jobId =>
      TestService
        .calculatePrimes(jobId, _10Sec)(global)
    }.toList
    Thread.sleep(1000)
    val futures2 = (4 to 24).map { jobId =>
      TestService
        .threadSleepBlocking(jobId, 15)(blockingContext)
    }.toList

    (futures1 ++ futures2).map(future => Await.ready(future, Duration.Inf))
  }

  it should "run sleeping futures on global thread along with cpu intensive on dedicated thread pool" in {
    val blockingContext =
      ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
    val futures2 = (1 to 21).map { jobId =>
      TestService
        .threadSleepBlocking(jobId, 15)(blockingContext)
    }.toList
    Thread.sleep(1000)
    val futures1 = (22 to 24).map { jobId =>
      TestService
        .calculatePrimes(jobId, _10Sec)(global)
    }.toList

    (futures1 ++ futures2).map(future => Await.ready(future, Duration.Inf))
  }

}
