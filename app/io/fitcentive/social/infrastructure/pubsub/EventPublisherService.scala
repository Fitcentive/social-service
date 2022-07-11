package io.fitcentive.social.infrastructure.pubsub

import io.fitcentive.registry.events.push.UserFollowRequested
import io.fitcentive.registry.events.user.UserFollowRequestDecision
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

  override def publishUserFollowRequestDecision(targetUser: UUID, isApproved: Boolean): Future[Unit] =
    UserFollowRequestDecision(targetUser, isApproved)
      .pipe(publisher.publish(publisherConfig.userFollowRequestDecisionTopic, _))

  override def publishUserFollowRequestNotification(requestingUser: UUID, targetUser: UUID): Future[Unit] =
    UserFollowRequested(requestingUser, targetUser)
      .pipe(publisher.publish(publisherConfig.userFollowRequestedTopic, _))

}
