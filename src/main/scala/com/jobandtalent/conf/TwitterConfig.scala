package com.jobandtalent.conf

import com.typesafe.config.ConfigFactory

object TwitterConfig {
  val conig = ConfigFactory.load()
  val bearerToken = conig.getString("twitter.bearer-token")
}
