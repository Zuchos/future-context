package futures.context

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.FlatSpec

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class Futures_5 extends FlatSpec with LazyLogging {

  def logPrimeCalculationCompleted(
    jobId: Int
  )(implicit executionContext: ExecutionContext): Future[Unit] =
    Future(logger.info(s"${jobId} - prime calculation status: completed"))

  def doAfterPrimeCalculation(jobId: Int, future: => Future[Seq[Int]])(
    implicit executionContext: ExecutionContext
  ): Unit = {
    future.flatMap { _ =>
      logPrimeCalculationCompleted(jobId)
    }
  }

  def storeToDb(jobId: Int, primes: Seq[Int]): Future[Unit] = {
    logger.info(s"$jobId - stored to db")
    Future.unit
  }

  it should "Pitfall 4 - future as param" in {
    implicit val ec = scala.concurrent.ExecutionContext.global
    def calculatePrimeFuture = TestService.calculatePrimes(1, 20)
    doAfterPrimeCalculation(2, calculatePrimeFuture)
    doAfterPrimeCalculation(3, Future.successful(List(1, 2, 3)))
    doAfterPrimeCalculation(4, calculatePrimeFuture.map(_.filter(_ > 10)))
    doAfterPrimeCalculation(
      5,
      calculatePrimeFuture.flatMap(
        primes => storeToDb(6, primes).map(_ => primes)
      )
    )

    //vs
    for {
      primes <- TestService.calculatePrimes(1, 10)
      _ <- logPrimeCalculationCompleted(2)
//      _ <- Future { logPrimeCalculationCompleted(2) }
      _ <- storeToDb(3, primes)
    } yield {
      primes.filter(_ > 10)
    }
    Thread.sleep(5000)
  }

  it should "Pitfall 5 - future as field" in {
    implicit val ec = scala.concurrent.ExecutionContext.global
    val futureInt = Future {
      Thread.sleep(1000)
      42
    }
    Thread.sleep(1000)
    Future {
      Thread.sleep(5000)
    }
    futureInt.foreach(i => logger.info(s"Value of the int: ${i}"))
    Thread.sleep(10000)
  }

}
