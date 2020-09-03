package ru.github.sd1ver

import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization._

object JsonParser extends App {

  private val JsonStart = '{'
  private val JsonEnd = '}'

  implicit val formats: DefaultFormats.type = DefaultFormats

  case class Item(a: Int, b: Int)
  case class Answer(a: Int, b: Int, sum: Int)

  val data     = """[ { "a": 1, "b": 2}, { "a": 2, "b":3}, { "a": 5, "b": 7 }]"""
  val lazyData = data.to(LazyList)
  val answers = readAndCreateAnswer(lazyData)
  println(answers.toList.mkString("\n"))

  private def readAndCreateAnswer(stream: LazyList[Char]): LazyList[Answer] = {
    val jsonStart = stream.dropWhile(c => c != JsonStart)
    stream.headOption.map { _ =>
      val jsonItem   = jsonStart.takeWhile(c => c != JsonEnd).toList :+ JsonEnd
      val jsonString = jsonItem.mkString
      val tailStream = jsonStart.tail.dropWhile(c => c != JsonStart)
      val answer     = jsonItemToAnswer(jsonString)
      answer #:: readAndCreateAnswer(tailStream)
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



}
