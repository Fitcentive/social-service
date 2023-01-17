package io.fitcentive.social.services

import com.google.inject.ImplementedBy
import io.fitcentive.social.infrastructure.pubsub.EventPublisherService

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[EventPublisherService])
trait MessageBusService {
  def publishUserFriendRequestNotification(requestingUser: UUID, targetUser: UUID): Future[Unit]
  def publishUserFriendRequestDecision(targetUser: UUID, isApproved: Boolean): Future[Unit]
  def publishUserCommentedOnPostNotification(
    commentingUser: UUID,
    targetUser: UUID,
    postId: UUID,
    postCreatorId: UUID
  ): Future[Unit]
  def publishUserLikedPostNotification(likingUser: UUID, targetUser: UUID, postId: UUID): Future[Unit]
}
