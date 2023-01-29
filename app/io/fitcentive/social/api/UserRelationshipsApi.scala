package io.fitcentive.social.api

import cats.data.EitherT
import io.fitcentive.sdk.error.{DomainError, EntityNotFoundError}
import io.fitcentive.social.domain.{PublicUserProfile, User, UserFollowRequest, UserFollowStatus}
import io.fitcentive.social.repositories.{SocialMediaRepository, UserRelationshipsRepository}
import io.fitcentive.social.services.{MessageBusService, UserService}

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserRelationshipsApi @Inject() (
  userRelationshipsRepository: UserRelationshipsRepository,
  userService: UserService,
  messageBusService: MessageBusService,
)(implicit ec: ExecutionContext) {

  /**
    * The following are captured implicitly by detach deleting user nodes
    * 1. User liked posts
    * 2. User following others
    * 3. Other users following User
    */
  def deleteUser(userId: UUID): Future[Unit] =
    userRelationshipsRepository.deleteUser(userId)

  def upsertUser(publicUser: PublicUserProfile): Future[PublicUserProfile] =
    userRelationshipsRepository.upsertUser(publicUser)

  def requestToFriendUser(currentUserId: UUID, targetUserId: UUID): Future[Either[DomainError, Unit]] =
    (for {
      _ <- EitherT[Future, DomainError, Unit](userService.requestToFriendUser(currentUserId, targetUserId))
      _ <-
        EitherT.right[DomainError](messageBusService.publishUserFriendRequestNotification(currentUserId, targetUserId))
    } yield ()).value

  def applyUserFriendRequestDecision(
    targetUserId: UUID,
    requestingUserId: UUID,
    isRequestApproved: Boolean
  ): Future[Either[DomainError, Unit]] =
    (for {
      _ <- EitherT[Future, DomainError, UserFollowRequest](
        userService
          .getUserFriendRequest(requestingUserId, targetUserId)
          .map(_.map(Right.apply).getOrElse(Left(EntityNotFoundError("User follow request not found!"))))
      )
      _ <- EitherT[Future, DomainError, Unit](userService.deleteUserFriendRequest(requestingUserId, targetUserId))
      _ <- EitherT.right[DomainError] {
        if (isRequestApproved) userRelationshipsRepository.makeUserFriendsWithOther(requestingUserId, targetUserId)
        else Future.unit
      }
      _ <-
        EitherT.right[DomainError](messageBusService.publishUserFriendRequestDecision(targetUserId, isRequestApproved))
    } yield ()).value

  def unfriendUser(currentUserId: UUID, targetUserId: UUID): Future[Unit] =
    userRelationshipsRepository.makeUserUnfriendOther(currentUserId, targetUserId)

  def getUserFriends(currentUserId: UUID, skip: Int, limit: Int): Future[Seq[PublicUserProfile]] =
    userRelationshipsRepository.getUserFriends(currentUserId, skip, limit)

  def searchUserFriends(currentUserId: UUID, query: String, skip: Int, limit: Int): Future[Seq[PublicUserProfile]] =
    userRelationshipsRepository.searchUserFriends(currentUserId, query, skip, limit)

  def getUserFriendStatus(currentUserId: UUID, otherUserId: UUID): Future[UserFollowStatus] =
    for {
      isCurrentUserFriendsWithOtherUser <-
        userRelationshipsRepository
          .getUserIfFriendsWithOtherUser(currentUserId, otherUserId)
          .map(_.isDefined)
      hasCurrentUserRequestedToFriendOtherUser <-
        userService
          .getUserFriendRequest(currentUserId, otherUserId)
          .map(_.isDefined)
      hasOtherUserRequestedToFriendCurrentUser <-
        userService
          .getUserFriendRequest(otherUserId, currentUserId)
          .map(_.isDefined)
    } yield UserFollowStatus(
      currentUserId,
      otherUserId,
      isCurrentUserFriendsWithOtherUser,
      hasCurrentUserRequestedToFriendOtherUser,
      hasOtherUserRequestedToFriendCurrentUser
    )

}
