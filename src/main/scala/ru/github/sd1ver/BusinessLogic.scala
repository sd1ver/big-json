package ru.github.sd1ver

trait BusinessLogic {
  def toAnswer(item: Item): Answer = {
    val sum = item.a + item.b
    Answer(item.a, item.b, sum)
  }
}
