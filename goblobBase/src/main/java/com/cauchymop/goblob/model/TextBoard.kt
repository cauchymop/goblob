package com.cauchymop.goblob.model

import com.cauchymop.goblob.proto.PlayGameData
import java.io.IOException
import java.io.StringReader

import com.cauchymop.goblob.proto.PlayGameData.Color

/**
 * Class to convert between a [GoBoard] and its text representation.
 */
object TextBoard {

  fun toString(board: GoBoard): String {
    val buf = StringBuilder()
    for (y in 0 until board.size) {
      for (x in 0 until board.size) {
        buf.append(getChar(board.getColor(x, y)))
      }
      buf.append('\n')
    }
    return buf.toString()
  }

  private fun getChar(color: Color?): Char {
    if (color == null) {
      return '.'
    }
    when (color) {
      PlayGameData.Color.BLACK -> return '●'
      PlayGameData.Color.WHITE -> return '○'
    }
  }

  fun fillBoard(board: GoBoard, text: String) {
    var x = 0
    var y = 0
    val reader = StringReader(text)
    while (true) {
      try {
        when (reader.read()) {
          -1 -> return
          '#'.toInt() -> skipLine(reader)
          '\n'.toInt() -> {
            y++
            x = 0
          }
          '.'.toInt() -> x++
          '●'.toInt(), 'X'.toInt() -> {
            board.play(Color.BLACK, board.getPos(x, y))
            x++
          }
          '○'.toInt(), 'O'.toInt() -> {
            board.play(Color.WHITE, board.getPos(x, y))
            x++
          }
        }
      } catch (e: IOException) {
        // A StringReader can't throw an IOException.
      }

    }
  }

  @Throws(IOException::class)
  private fun skipLine(reader: StringReader) {
    var c: Int
    do {
      c = reader.read()
      if (c == -1) {
        return
      }
    } while (c != '\n'.toInt())
  }
}

fun GoBoard.fill(boardString: String) {
  TextBoard.fillBoard(this, boardString)
}
