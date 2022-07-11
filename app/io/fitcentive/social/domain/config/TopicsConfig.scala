package io.fitcentive.social.domain.config

import com.typesafe.config.Config
import io.fitcentive.sdk.config.PubSubTopicConfig

case class TopicsConfig(userFollowRequestedTopic: String, userFollowRequestDecisionTopic: String)
  extends PubSubTopicConfig {

  val topics: Seq[String] =
    Seq(userFollowRequestedTopic, userFollowRequestDecisionTopic)

}

object TopicsConfig {
  def fromConfig(config: Config): TopicsConfig =
    TopicsConfig(config.getString("user-follow-requested"), config.getString("user-follow-request-decision"))
}
