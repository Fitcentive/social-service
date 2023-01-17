package io.fitcentive.social.domain.config

import com.typesafe.config.Config
import io.fitcentive.sdk.config.PubSubTopicConfig

case class TopicsConfig(
  userFriendRequestedTopic: String,
  userFriendRequestDecisionTopic: String,
  userCommentedOnPostTopic: String,
  userLikedPostTopic: String
) extends PubSubTopicConfig {

  val topics: Seq[String] =
    Seq(userFriendRequestedTopic, userFriendRequestDecisionTopic, userCommentedOnPostTopic, userLikedPostTopic)

}

object TopicsConfig {
  def fromConfig(config: Config): TopicsConfig =
    TopicsConfig(
      config.getString("user-friend-requested"),
      config.getString("user-friend-request-decision"),
      config.getString("user-commented-on-post"),
      config.getString("user-liked-post"),
    )
}
