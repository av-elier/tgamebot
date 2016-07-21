package avelier.tgamebot

import info.mukel.telegrambot4s._
import api._
import methods._
import models._
import Implicits._
import avelier.tgamebot.config.Settings

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

  def main(args: Array[String]): Unit = {
    EchoBot.run()
  }
}
