package io.fitcentive.social.domain.config

import com.typesafe.config.Config
import io.fitcentive.sdk.config.PubSubTopicConfig

case class TopicsConfig(
  userFollowRequestedTopic: String,
  userFollowRequestDecisionTopic: String,
  userCommentedOnPostTopic: String,
  userLikedPostTopic: String
) extends PubSubTopicConfig {

  val topics: Seq[String] =
    Seq(userFollowRequestedTopic, userFollowRequestDecisionTopic, userCommentedOnPostTopic, userLikedPostTopic)

}

object TopicsConfig {
  def fromConfig(config: Config): TopicsConfig =
    TopicsConfig(
      config.getString("user-follow-requested"),
      config.getString("user-follow-request-decision"),
      config.getString("user-commented-on-post"),
      config.getString("user-liked-post"),
    )
}
