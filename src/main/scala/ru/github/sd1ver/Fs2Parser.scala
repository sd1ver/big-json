package ru.github.sd1ver

import java.nio.file.Paths
import java.util.concurrent.Executors

import cats.effect.{Blocker, ContextShift, IO}
import fs2.Stream
import io.circe.generic.auto._
import io.circe.syntax._
import ru.github.sd1ver.data.Item
import scala.language.postfixOps

import scala.concurrent.ExecutionContext

object Fs2Parser extends App with BusinessLogic with JsonSyntax{

  val blockingPool                                = Executors.newFixedThreadPool(1)
  implicit val executionContext: ExecutionContext = ExecutionContext.fromExecutor(blockingPool)
  implicit val contextShift: ContextShift[IO]     = IO.contextShift(executionContext)

  val inputFile  = "example.json"
  val outputFile = "fs2_output.json"

  circeParser.compile.toList.unsafeRunSync()
  blockingPool.shutdown()

  private def circeParser = {
    Stream.resource(Blocker[IO]).flatMap { blocker =>
      val fileBytes = fs2.io.file.readAll[IO](Paths.get(inputFile), blocker, 4096)
      val jsonStream = fileBytes
        .through(io.circe.fs2.byteArrayParser)
        .through(io.circe.fs2.decoder[IO, Item])
        .map(toAnswer)
        .map(i => i.asJson.toString)
        .zipWithIndex
        .map(addCommaAfterFirst _ tupled)
        .flatMap(b => Stream.emits(b.getBytes))
      val result = Stream[IO, Byte](JsonArrayStart.toByte) ++ jsonStream ++ Stream[IO, Byte](JsonArrayEnd.toByte)
      result.through(fs2.io.file.writeAll(Paths.get(outputFile), blocker))
    }
  }

  def addCommaAfterFirst(json: String, index: Long): String = {
    val separator = if(index > 0) JsonArraySeparator else ""
    separator + json
  }

}
