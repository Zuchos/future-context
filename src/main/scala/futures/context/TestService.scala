package futures.context

import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future, blocking}

object TestService extends LazyLogging {

  def isPrime(i: Int): Boolean = {
    if (i <= 1)
      false
    else if (i == 2)
      true
    else
      !(2 until i).exists(x => i % x == 0)
  }

  def calculatePrimes(jobId: Int, till: Int = 10000)(
    implicit executionContext: ExecutionContext
  ): Future[Seq[Int]] = {
    Future {
      logger.info(s"$jobId - Starting calculating primes till $till")
      val r = (0 to till).filterNot(isPrime)
      logger.info(s"$jobId - Finished")
      r
    }
  }

  def threadSleep(jobId: Int, numberOfSec: Int = 30)(
    implicit executionContext: ExecutionContext
  ): Future[Unit] = {
    Future {
      logger.info(s"$jobId - Staring sleeping thread")
      Thread.sleep(1000 * numberOfSec)
      logger.info(s"$jobId - Finished")
    }
  }

  def threadSleepBlocking(jobId: Int, numberOfSec: Int = 30)(
    implicit executionContext: ExecutionContext
  ): Future[Unit] = {
    Future {
      blocking {
        logger.info(s"$jobId - Started blocking sleeping thread")
        Thread.sleep(1000 * numberOfSec)
        logger.info(s"$jobId - Finished")
      }
    }
  }
}
