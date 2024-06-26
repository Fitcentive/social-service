package io.fitcentive.social.infrastructure.pubsub

import io.fitcentive.registry.events.push.UserFriendRequested
import io.fitcentive.registry.events.social.{UserCommentedOnPost, UserLikedPost}
import io.fitcentive.registry.events.user.UserFriendRequestDecision
import io.fitcentive.sdk.gcp.pubsub.PubSubPublisher
import io.fitcentive.social.domain.config.TopicsConfig
import io.fitcentive.social.infrastructure.contexts.PubSubExecutionContext
import io.fitcentive.social.services.{MessageBusService, SettingsService}

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.util.chaining.scalaUtilChainingOps

@Singleton
class EventPublisherService @Inject() (publisher: PubSubPublisher, settingsService: SettingsService)(implicit
  ec: PubSubExecutionContext
) extends MessageBusService {

  private val publisherConfig: TopicsConfig = settingsService.pubSubConfig.topicsConfig

  override def publishUserCommentedOnPostNotification(
    commentingUser: UUID,
    targetUser: UUID,
    postId: UUID,
    postCreatorId: UUID,
  ): Future[Unit] =
    UserCommentedOnPost(commentingUser, targetUser, postId, postCreatorId)
      .pipe(publisher.publish(publisherConfig.userCommentedOnPostTopic, _))

  override def publishUserLikedPostNotification(likingUser: UUID, targetUser: UUID, postId: UUID): Future[Unit] =
    UserLikedPost(likingUser, targetUser, postId)
      .pipe(publisher.publish(publisherConfig.userLikedPostTopic, _))

  override def publishUserFriendRequestDecision(targetUser: UUID, isApproved: Boolean): Future[Unit] =
    UserFriendRequestDecision(targetUser, isApproved)
      .pipe(publisher.publish(publisherConfig.userFriendRequestDecisionTopic, _))

  override def publishUserFriendRequestNotification(requestingUser: UUID, targetUser: UUID): Future[Unit] =
    UserFriendRequested(requestingUser, targetUser)
      .pipe(publisher.publish(publisherConfig.userFriendRequestedTopic, _))

}
