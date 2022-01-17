package futures.context

import com.typesafe.scalalogging.LazyLogging
import futures.context.TestConstants._
import org.scalatest.FlatSpec

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future, blocking}

class Futures_4 extends FlatSpec with LazyLogging {

  /**
    * Future in yield
    */
  it should "Pitfall 1" in {
    implicit val ec = scala.concurrent.ExecutionContext.global
    val future = for {
      _ <- TestService.threadSleep(1, 3)
      _ <- TestService.calculatePrimes(2, 100)
    } yield {
      TestService.threadSleep(3, 3)
    }
    Await.ready(future, Duration.Inf)
    println("Future completed")
    Thread.sleep(6000)
  }

  /**
    * Future.successful doesn't need a thread
    * Mention Future.unit
    */
  it should "Pitfall 2" in {
    val ec =
      ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
    TestService.threadSleep(1, 5)(ec)
    val f1 = Future {
      logger.info("F1 dit it!")
    }(ec)
    val f2 = Future.successful {
      logger.info("F2 dit it!")
    }
    Thread.sleep(6000)
  }

  /**
    * assignments in for {} should not be blocking (it's a body of flatMap/map after all)
    */
  it should "Pitfall 3" in {
    implicit val ec =
      ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())
    val future1 = for {
      _ <- TestService.calculatePrimes(10, 100)
      _ = {
        logger.info("Going asleep")
        Thread.sleep(2000)
        logger.info("Waking up")
      }
    } yield {
      logger.info("We did it!")
    }
    Thread.sleep(1000)
    val future2 = Future { logger.info("Ooops, We did it again!") }
    Thread.sleep(4000)
  }
}
