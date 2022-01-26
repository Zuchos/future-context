package futures.context

import futures.context.TestConstants._10Sec
import org.scalatest.FlatSpec

import scala.concurrent.ExecutionContext.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

class Futures_2 extends FlatSpec {

  it should "run cpu intensive blocking features along with sleeping futures on global thread pool" in {
    val futures1 = (1 to 3).map { jobId =>
      TestService
        .calculatePrimes(jobId, _10Sec)(global)
    }.toList
    Thread.sleep(1000)
    val futures2 = (4 to 24).map { jobId =>
      TestService
        .threadSleepBlocking(jobId, 15)(global)
    }.toList
    (futures1 ++ futures2).map(future => Await.ready(future, Duration.Inf))
  }

  it should "run sleeping futures on global thread along with cpu intensive blocking features pool" in {
    val futures2 = (1 to 21).map { jobId =>
      TestService
        .threadSleepBlocking(jobId, 15)(global)
    }.toList
    Thread.sleep(1000)
    val futures1 = (1 to 3).map { jobId =>
      TestService
        .calculatePrimes(jobId, _10Sec)(global)
    }.toList

    (futures1 ++ futures2).map(future => Await.ready(future, Duration.Inf))
  }

  /**
    * What happened?
    * Parallelism level is equal to cores (by default)
    * The number of concurrently blocking computations can exceed the parallelism level ONLY
    * if each blocking (every future) call is wrapped inside a blocking call (more on that below).
    * Otherwise, there is a risk that the thread pool in the global execution context is starved, and no computation can proceed.
    *  If you need to wrap long lasting blocking operations we recommend using a dedicated ExecutionContext, for instance by wrapping a Java Executor.
    */

  /**
    * Q: What do we gain by using blocking?
    */

  /**
    * A: We are avoiding situation that only blocking calls are taking threads from global thread pool and there is no way for program to move forward - deadlock.
    * e.g. 16 threads are checking if file exists on disk, but the thread that is writing that file is waiting to be executed.
    * However we need to be careful no to spawn to many threads.
    */

  /**
    * Q: How about our refactoring? Would it help to use blocking and global execution context?
    */

  /**
  * A: Kind of ... we won't deadlock/block program from running by performing blocking operations (e.g. hadoop read/writes). But we can still slow it down (but not as much as before).
  * We may also slow down other parts of application/platform since the global execution context is used by other components.
  */
}
