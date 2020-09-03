package ru.github.sd1ver

import java.io.{ BufferedWriter, Closeable, File, FileWriter }

import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization._

import scala.io.Source

object JsonParser extends App {

  private val JsonStart          = '{'
  private val JsonEnd            = '}'
  private val JsonArrayStart     = '['
  private val JsonArrayEnd       = ']'
  private val JsonArraySeparator = ','

  private val inBufferSize  = 10
  private val outBufferSize = 10

  implicit val formats: DefaultFormats.type = DefaultFormats

  case class Item(a: Int, b: Int)
  case class Answer(a: Int, b: Int, sum: Int)

  val outFile = new File("output.json")
  val writer  = new BufferedWriter(new FileWriter(outFile))

  def inputFile = Source.fromFile(new File("example.json"), inBufferSize)

  withClosable(inputFile) { source =>
    withClosable(new FileWriter(outFile)) { fileWriter =>
      withClosable(new BufferedWriter(fileWriter, outBufferSize)) { bufferedWriter =>
        val fileData    = source.to(LazyList)
        val fileAnswers = convertToAnswers(fileData)
        bufferedWriter.append(JsonArrayStart)
        fileAnswers.foreach { answer =>
          val jsonAnswer = write(answer)
          bufferedWriter.write(jsonAnswer)
          bufferedWriter.append(JsonArraySeparator)
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

  private def toAnswer(item: Item): Answer = {
    val sum = item.a + item.b
    Answer(item.a, item.b, sum)
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
