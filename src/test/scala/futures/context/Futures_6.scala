package futures.context

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.FlatSpec

import java.util.concurrent.Executors
import scala.collection.immutable
import scala.concurrent._
import scala.concurrent.duration._

class Futures_6 extends FlatSpec with LazyLogging {

  /**
    * Future are eager
    */
  it should "Pitfall 6" in {
    implicit val ec =
      ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
    val future2 = Future { logger.info("Ooops, We did it again!") }
    val future1 =
      for {
        _ <- TestService.threadSleep(10, 1)
      } yield {
        logger.info("We did it!")
      }
    for {
      _ <- future1
      _ <- future2
    } yield {
      logger.info("We did all the work!")
    }
    Thread.sleep(3000)
  }

  it should "multiple operations" in {
    //Parallel
    implicit val ec =
      ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))
    val futures1 = (1 to 10).map { i =>
      TestService.threadSleep(i, 5)
    }
    Await.result(Future.sequence(futures1), Duration.Inf)

    //vs Sequential
    val futures2 = (11 to 20).toList.foldLeft(Future.unit) {
      case (acc, e) => acc.flatMap(_ => TestService.threadSleep(e, 1))
    }
    Await.ready(futures2, Duration.Inf)

    //Beware
    //Future.foldLeft()
    //Future.traverse() (quite similar to Future.sequence(List.map(_ => Future()))
  }
}
