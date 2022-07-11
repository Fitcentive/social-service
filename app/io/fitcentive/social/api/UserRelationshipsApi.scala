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

  def upsertUser(publicUser: PublicUserProfile): Future[PublicUserProfile] =
    userRelationshipsRepository.upsertUser(publicUser)

  def requestToFollowUser(currentUserId: UUID, targetUserId: UUID): Future[Either[DomainError, Unit]] =
    (for {
      _ <- EitherT[Future, DomainError, Unit](userService.requestToFollowUser(currentUserId, targetUserId))
      _ <-
        EitherT.right[DomainError](messageBusService.publishUserFollowRequestNotification(currentUserId, targetUserId))
    } yield ()).value

  def applyUserFollowRequestDecision(
    targetUserId: UUID,
    requestingUserId: UUID,
    isRequestApproved: Boolean
  ): Future[Either[DomainError, Unit]] =
    (for {
      _ <- EitherT[Future, DomainError, UserFollowRequest](
        userService
          .getUserFollowRequest(requestingUserId, targetUserId)
          .map(_.map(Right.apply).getOrElse(Left(EntityNotFoundError("User follow request not found!"))))
      )
      _ <- EitherT[Future, DomainError, Unit](userService.deleteUserFollowRequest(requestingUserId, targetUserId))
      _ <- EitherT.right[DomainError] {
        if (isRequestApproved) userRelationshipsRepository.makeUserFollowOther(requestingUserId, targetUserId)
        else Future.unit
      }
      _ <-
        EitherT.right[DomainError](messageBusService.publishUserFollowRequestDecision(targetUserId, isRequestApproved))
    } yield ()).value

  def removeFollowerForUser(currentUserId: UUID, followingUserId: UUID): Future[Unit] =
    userRelationshipsRepository.removeFollowerForUser(currentUserId, followingUserId)

  def unfollowUser(currentUserId: UUID, targetUserId: UUID): Future[Unit] =
    userRelationshipsRepository.makeUserUnFollowOther(currentUserId, targetUserId)

  def getUserFollowers(currentUserId: UUID): Future[Seq[PublicUserProfile]] =
    userRelationshipsRepository.getUserFollowers(currentUserId)

  def getUserFollowing(currentUserId: UUID): Future[Seq[PublicUserProfile]] =
    userRelationshipsRepository.getUserFollowing(currentUserId)

  def getUserFollowStatus(currentUserId: UUID, otherUserId: UUID): Future[UserFollowStatus] =
    for {
      isCurrentUserFollowingOtherUser <-
        userRelationshipsRepository
          .getUserIfFollowingOtherUser(currentUserId, otherUserId)
          .map(_.isDefined)
      isOtherUserFollowingCurrentUser <-
        userRelationshipsRepository
          .getUserIfFollowingOtherUser(otherUserId, currentUserId)
          .map(_.isDefined)
      hasCurrentUserRequestedToFollowOtherUser <-
        userService
          .getUserFollowRequest(currentUserId, otherUserId)
          .map(_.isDefined)
      hasOtherUserRequestedToFollowCurrentUser <-
        userService
          .getUserFollowRequest(otherUserId, currentUserId)
          .map(_.isDefined)
    } yield UserFollowStatus(
      currentUserId,
      otherUserId,
      isCurrentUserFollowingOtherUser,
      isOtherUserFollowingCurrentUser,
      hasCurrentUserRequestedToFollowOtherUser,
      hasOtherUserRequestedToFollowCurrentUser
    )

}
