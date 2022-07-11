package io.fitcentive.social.services

import com.google.inject.ImplementedBy
import io.fitcentive.social.infrastructure.pubsub.EventPublisherService

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[EventPublisherService])
trait MessageBusService {
  def publishUserFollowRequestNotification(requestingUser: UUID, targetUser: UUID): Future[Unit]
  def publishUserFollowRequestDecision(targetUser: UUID, isApproved: Boolean): Future[Unit]
}
