package io.fitcentive.social.domain.config

import io.fitcentive.sdk.config.PubSubConfig

case class AppPubSubConfig(topicsConfig: TopicsConfig, subscriptionsConfig: SubscriptionsConfig) extends PubSubConfig
