package ru.github.sd1ver

import java.nio.file.Paths
import java.util.concurrent.Executors

import cats.effect.{Blocker, ContextShift, IO}
import fs2.{Chunk, Stream}
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.ExecutionContext

object Fs2Parser extends App with BusinessLogic {

  val blockingPool                                = Executors.newFixedThreadPool(1)
  implicit val executionContext: ExecutionContext = ExecutionContext.fromExecutor(blockingPool)
  implicit val contextShift: ContextShift[IO]     = IO.contextShift(executionContext)

  val inputFile = "example.json"
  val outputFile = "fs2_output.json"

  circeParser.compile.toList.unsafeRunSync()
  blockingPool.shutdown()

  private def circeParser = {
    Stream.resource(Blocker[IO]).flatMap { blocker =>
      val fileBytes = fs2.io.file.readAll[IO](Paths.get(inputFile), blocker, 4096)
      val result = fileBytes
        .through(io.circe.fs2.byteArrayParser)
        .through(io.circe.fs2.decoder[IO, Item])
        .map(toAnswer)
        .map(i => i.asJson.toString.getBytes)
        .flatMap(b => Stream.chunk(Chunk.array(b)))
      result
          .through(fs2.io.file.writeAll(Paths.get(outputFile), blocker))
    }
  }

}
