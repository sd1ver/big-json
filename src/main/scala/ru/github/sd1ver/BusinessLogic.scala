package ru.github.sd1ver

import ru.github.sd1ver.data.{Answer, Item}

trait BusinessLogic {
  def toAnswer(item: Item): Answer = {
    val sum = item.a + item.b
    Answer(item.a, item.b, sum)
  }
}
