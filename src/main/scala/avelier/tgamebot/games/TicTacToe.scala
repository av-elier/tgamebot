package avelier.tgamebot.games

import java.util.logging.Logger

import info.mukel.telegrambot4s.methods.{ApiRequest, ParseMode, SendMessage}
import info.mukel.telegrambot4s.models.{KeyboardButton, Message, ReplyKeyboardMarkup}
import info.mukel.telegrambot4s.Implicits._

import scala.collection.mutable
import scala.util.Random

/**
  * Created by Adelier on 22.07.2016.
  */
class TicTacToe(chatId: Long) extends TGameSingleInstance(chatId: Long) {
  sealed trait Mark {
    def other: Mark
  }
  object Cross extends Mark {
    override def other: Mark = Nought
    override def toString = "Cross"
  }
  object Nought extends Mark {
    override def other: Mark = Cross
    override def toString = "Nought"
  }

  class TicTacToeBoard(val state: mutable.Seq[mutable.Seq[Option[Mark]]] = mutable.Seq.fill(3,3)(None)) {
    def put(pos: (Int, Int), mark: Mark): Boolean = {
      Logger.getGlobal.info(s"putting $pos")
      state(pos._1)(pos._2) match {
        case Some(_) => false
        case None =>
          state(pos._1)(pos._2) = Some(mark)
          true
      }
    }

    def winner: Option[Mark] = {
      val winningLines: Seq[Seq[(Int,Int)]] = {
        val winningDiagonals = Seq((0 to 2).map(i => (i,i)), (0 to 2).map(i => (2-i,i)))
        val winningHorizontalAndVerticalLines = {for {
          i <- 0 to 2
        } yield Seq(Seq((0,i), (1,i), (2,i)), Seq((i,0), (i,1), (i,2)))}.flatten
        winningDiagonals ++ winningHorizontalAndVerticalLines
      }
      val winner: Option[Mark] = winningLines.flatMap(line => {
        line.map{case(x,y) => state(x)(y)}
            .reduce[Option[Mark]]{
              case (Some(Cross), Some(Cross)) => Some(Cross)
              case (Some(Nought), Some(Nought)) => Some(Nought)
              case (_, _) => None
            }
      }).headOption
      winner
    }

    def freeSpaces: Seq[(Int, Int)] = (for (x <- 0 to 2) yield {
      (for (y <- 0 to 2) yield {
        state(x)(y) match {
          case None => Some(x, y)
          case Some(_) => None
        }
      }).flatten
    }).flatten

    override def toString = {
      val res = new StringBuffer
      for (row <- state) {
        for (place <- row) place match {
          case Some(Cross) => res append "x"
          case Some(Nought) => res append "o"
          case None => res append "."
        }
        res append "\n"
      }
      res.toString
    }
  }

  var game = new TicTacToeBoard()
  var playerMark: Mark = Cross

  def responseStateWithKeyboard(keyb: Seq[KeyboardButton]*): Seq[ApiRequest[Message]] = {
    Seq(SendMessage(chatId, s"```\n$game\n```", parseMode = ParseMode.Markdown, replyMarkup = ReplyKeyboardMarkup(keyb, oneTimeKeyboard = true)))
  }
  def responseStateWithDefaultKeyboard: Seq[ApiRequest[Message]] = {
    val positions = for (x <- 0 to 2) yield { // TODO: use improved freeSpaces
      for (y <- 0 to 2) yield game.state(x)(y) match {
        case None => s"$x, $y"
        case Some(_) => "_"
      }
    }
    responseStateWithKeyboard(
      positions.map(line => line.map(s => KeyboardButton(s))):_*
    )
  }

  override def name = "Tic Tac Toe"
  override def start() = {
    responseStateWithKeyboard(
      Seq(KeyboardButton("x"), KeyboardButton("o"))
    )
  }
  override def cancel() = Seq()
  override def handle(m: Message): Seq[ApiRequest[Message]] = {
    val posPattern = """(\d),\s*(\d)""".r
    m.text match {
      case Some("x") => playerMark = Cross
      case Some("o") => playerMark = Nought
      case Some(posPattern(x, y)) =>
        Logger.getGlobal.info(s"posPattern $x, $y")
        // human turn
        val success = game.put((x.toInt, y.toInt), playerMark)
        if (!success) {
          return Seq(SendMessage(chatId, s"Wrong position ($x, $y)")) ++ responseStateWithDefaultKeyboard
        }

        game.winner.foreach{m => {
          isEnded = true
          return responseStateWithDefaultKeyboard ++ Seq(SendMessage(chatId, s"Game ended! Winner is $m"))
        }}
        val freeSpaces = game.freeSpaces
        if (freeSpaces.isEmpty) {
          isEnded = true
          return responseStateWithDefaultKeyboard ++ Seq(SendMessage(chatId, s"Game ended! No winner"))
        }

        // bot turn
        val (botX, botY) = freeSpaces(Random.nextInt(freeSpaces.length))
        game.put((botX, botY), playerMark.other)

        game.winner.foreach{m => {
          isEnded = true
          return responseStateWithDefaultKeyboard ++ Seq(SendMessage(chatId, s"Game ended! Winner is $m"))
        }}

        if (game.freeSpaces.isEmpty) {
          isEnded = true
          return responseStateWithDefaultKeyboard ++ Seq(SendMessage(chatId, s"Game ended! No winner"))
        }
      case _ =>
        Logger.getGlobal.info("_______________")
        return responseStateWithDefaultKeyboard
    }
    responseStateWithDefaultKeyboard
  }

}
