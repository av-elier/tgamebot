package avelier.tgamebot

import info.mukel.telegrambot4s.api._
import info.mukel.telegrambot4s.methods._
import info.mukel.telegrambot4s.models._
import info.mukel.telegrambot4s.Implicits._
import avelier.tgamebot.config.Settings
import avelier.tgamebot.games.{TGame, TGameSingle, TicTacToe}

object Main {

  object EchoBot extends TelegramBot with Polling {
    override def token: String = Settings.botToken

    override def handleMessage(message: Message): Unit = {
      for (text <- message.text) {
        val markup = ReplyKeyboardMarkup(Seq( // TODO: maybe implicit def for String => KeyboardButton ?
          Seq(KeyboardButton("12"), KeyboardButton("23"), KeyboardButton("34")),
          Seq(KeyboardButton("45"), KeyboardButton("56"), KeyboardButton("67"))
        ), oneTimeKeyboard=true)
        api.request(SendMessage(message.chat.id, text.reverse, replyMarkup=markup))
      }
    }
  }
  object GameBot extends TelegramBot with Polling {
    override def token: String = Settings.botToken

    val game = new TGameSingle(chatId => new TicTacToe(chatId))

    override def handleMessage(message: Message): Unit = {
      for (text <- message.text) text match {
        case "/start" => game.start(message).foreach(req => api.request(req))
        case _ => game.handle(message).foreach(req => api.request(req))
      }
    }
  }

  def main(args: Array[String]): Unit = {
//    EchoBot.run()
    GameBot.run()
  }
}
