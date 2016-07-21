package avelier.tgamebot.config

import com.typesafe.config.ConfigFactory

object Settings {
  val config = ConfigFactory.load()

  val botToken = config.getString("telegram_bot.token")
}
