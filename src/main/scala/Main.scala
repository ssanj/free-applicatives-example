import java.time.LocalDateTime

import cats.Applicative
import cats.syntax.cartesian._
import monix.eval.Task
import monix.execution.schedulers.ExecutionModel.AlwaysAsyncExecution

import scala.concurrent.Await
import scala.concurrent.duration._

object Main {

  import TaskApplicativeInstance.TaskApplicative

  implicit val scheduler = monix.execution.Scheduler.fixedPool("appThreadPool", 10, executionModel = AlwaysAsyncExecution)

  def main(args: Array[String])  {
    log("Starting")
    val task1 = Task.apply { log("Started Action1"); 1 }.delayResult(5.seconds).map(x=> { log("Finished Action1"); x } )
    val task2 = Task.apply { log("Started Action2"); 2 }.map(x=> { log("Finished Action2"); x } )

    val task = (task1 |@| task2).map(_ + _)
    val result = Await.result(task.runAsync, 10.seconds)
    log(s"Result is $result")
    log("Finished")
  }

  private def log(s: String) = println(s"${LocalDateTime.now()} - $s")
}

object TaskApplicativeInstance {
  implicit val TaskApplicative: Applicative[Task] = new Applicative[Task] {
    override def pure[A](x: A): Task[A] = Task.now(x)

    override def ap[A, B](ff: Task[A => B])(fa: Task[A]): Task[B] = Task.mapBoth(ff, fa)((f, a) => f(a))
  }
}

