package io.fitcentive.social.domain.config

import com.typesafe.config.Config
import io.fitcentive.sdk.config.PubSubSubscriptionConfig

case class SubscriptionsConfig() extends PubSubSubscriptionConfig {
  val subscriptions: Seq[String] = Seq()
}

object SubscriptionsConfig {
  def fromConfig(config: Config): SubscriptionsConfig = SubscriptionsConfig()
}
