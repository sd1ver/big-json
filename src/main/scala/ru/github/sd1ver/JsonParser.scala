package ru.github.sd1ver

import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization._

/* Есть файл, в котором записан JSON [{ "a": int, "b": int }, { "a": int, "b": int }, ... ].
Сгенерировать файл, в котором будет лежать [ { "a": int, "b": int, "sum": int}, ... ]*/

object JsonParser extends App {

  private val JsonStart = '{'
  private val JsonEnd = '}'
  implicit val formats: DefaultFormats.type = DefaultFormats
  case class Item(a: Int, b: Int)

  case class Answer(a: Int, b: Int, sum: Int)

  val items = LazyList[Item]()

  val data     = """[ { "a": 1, "b": 2}, { "a": 2, "b":3}, { "a": 5, "b": 7 }]"""
  val lazyData = data.to(LazyList)

  private def reader(stream: LazyList[Char]): LazyList[Answer] = {
    val jsonStart = stream.dropWhile(c => c != JsonStart)
    stream.headOption.map { _ =>
      val jsonItem   = jsonStart.takeWhile(c => c != JsonEnd).toList :+ JsonEnd
      val jsonString = jsonItem.mkString
      val tailStream = jsonStart.tail.dropWhile(c => c != JsonStart)
      val answer     = jsonItemToAnswer(jsonString)
      answer #:: reader(tailStream)
    }.getOrElse(LazyList.empty)
  }

  def toAnswer(item: Item): Answer = {
    val sum = item.a + item.b
    Answer(item.a, item.b, sum)
  }

  def jsonItemToAnswer(json: String): Answer = {
    val item = read[Item](json)
    toAnswer(item)
  }

  val res = reader(lazyData)
  println(res.toList.mkString("\n"))

}
