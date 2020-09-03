package ru.github.sd1ver

import java.io.{BufferedWriter, Closeable, File, FileWriter}

import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization._
import ru.github.sd1ver.data.{Answer, Item}

import scala.io.Source

object JsonParser extends App with BusinessLogic with JsonSyntax{

  private val inBufferSize  = 10
  private val outBufferSize = 10

  implicit val formats: DefaultFormats.type = DefaultFormats

  val outFile = new File("output.json")
  val writer  = new BufferedWriter(new FileWriter(outFile))

  def inputFile = Source.fromFile(new File("example.json"), inBufferSize)

  withClosable(inputFile) { source =>
    withClosable(new FileWriter(outFile)) { fileWriter =>
      withClosable(new BufferedWriter(fileWriter, outBufferSize)) { bufferedWriter =>
        val fileData    = source.to(LazyList)
        val fileAnswers = convertToAnswers(fileData)
        bufferedWriter.append(JsonArrayStart)
        var hasWrittenAnswers = false
        fileAnswers.foreach { answer =>
          if (hasWrittenAnswers) {
            bufferedWriter.write(JsonArraySeparator)
          }
          val jsonAnswer = write(answer)
          bufferedWriter.write(jsonAnswer)
          hasWrittenAnswers = true
        }
        bufferedWriter.append(JsonArrayEnd)
      }
    }
  }

  private def convertToAnswers(stream: LazyList[Char]): LazyList[Answer] = {
    val jsonStart = stream.dropWhile(c => c != JsonStart)
    stream.headOption.map { _ =>
      val jsonItem   = jsonStart.takeWhile(c => c != JsonEnd).toList :+ JsonEnd
      val jsonString = jsonItem.mkString
      val tailStream = jsonStart.tail.dropWhile(c => c != JsonStart)
      val answer     = jsonItemToAnswer(jsonString)
      answer #:: convertToAnswers(tailStream)
    }.getOrElse(LazyList.empty)
  }

  private def jsonItemToAnswer(json: String): Answer = {
    val item = read[Item](json)
    toAnswer(item)
  }

  private def withClosable[A <: Closeable, B](closable: => A)(body: A => B) = {
    val resource = closable
    try {
      body(resource)
    } finally {
      resource.close()
    }
  }

}
