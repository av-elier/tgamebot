package avelier.tgamebot.games

import java.util.logging.Logger

import info.mukel.telegrambot4s.methods.{ApiRequest, SendMessage}
import info.mukel.telegrambot4s.models.{Chat, Message}
import info.mukel.telegrambot4s.Implicits._

import scala.collection.mutable

trait TGame {
  def start(m: Message): Seq[ApiRequest[Message]]
  def cancel(m: Message): Seq[ApiRequest[Message]]
  def handle(m: Message): Seq[ApiRequest[Message]]
  // TODO: generate 'response' without request. akka-streams?
}

abstract class TGameSingleInstance(val chatId: Long) {
  def name: String
  var isEnded = false
  def cancel(): Seq[ApiRequest[Message]]
  def start(): Seq[ApiRequest[Message]]
  def handle(m: Message): Seq[ApiRequest[Message]]
}
class TGameSingle(gameInstance: Long => TGameSingleInstance) extends TGame {
  type ChatId = Long
  val games = mutable.Map[ChatId, TGameSingleInstance]()

  def start(m: Message) = {
    if (games.contains(m.chat.id)) {
      Logger.getGlobal.info("msg.new")
      cancel(m)
      start(m)
    } else {
      Logger.getGlobal.info("msg.exist")
      val game = gameInstance(m.chat.id)
      games(m.chat.id) = game
      Seq(SendMessage(m.chat.id, s"Started game '${game.name}'")) ++ game.start()
    }
  }
  def cancel(m: Message) = {
    Logger.getGlobal.info("msg.calcel")
    val game = games(m.chat.id)
    games.remove(m.chat.id)
    game.cancel() ++ Seq(SendMessage(m.chat.id, s"Cancelled game '${game.name}'"))
  }
  def handle(m: Message): Seq[ApiRequest[Message]] = {
    try {
      Logger.getGlobal.info("msg.handle")
      games(m.chat.id).handle(m)
    } catch {
      case e: Exception => Seq(SendMessage(m.chat.id, s"Exception: $e"))
    }
  }
}

// TODO: TGameMulti (Chess for example)
