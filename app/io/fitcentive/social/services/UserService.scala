package io.fitcentive.social.services

import com.google.inject.ImplementedBy
import io.fitcentive.sdk.error.DomainError
import io.fitcentive.social.domain.{User, UserFollowRequest}
import io.fitcentive.social.infrastructure.rest.RestUserService

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[RestUserService])
trait UserService {
  def getUserFriendRequest(requestingUserId: UUID, targetUserId: UUID): Future[Option[UserFollowRequest]]
  def deleteUserFriendRequest(requestingUserId: UUID, targetUserId: UUID): Future[Either[DomainError, Unit]]
  def requestToFriendUser(currentUserId: UUID, targetUserId: UUID): Future[Either[DomainError, Unit]]
}
