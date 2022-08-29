package io.fitcentive.social.api

import cats.data.EitherT
import io.fitcentive.sdk.error.{DomainError, EntityConflictError, EntityNotAccessible, EntityNotFoundError}
import io.fitcentive.social.domain.responses.UsersWhoLikedPost
import io.fitcentive.social.domain.{Post, PostComment, PublicUserProfile}
import io.fitcentive.social.repositories.{SocialMediaRepository, UserRelationshipsRepository}
import io.fitcentive.social.services.MessageBusService

import java.time.LocalDateTime
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SocialMediaApi @Inject() (
  userRelationshipsRepository: UserRelationshipsRepository,
  socialMediaRepository: SocialMediaRepository,
  messageBusService: MessageBusService,
)(implicit ec: ExecutionContext) {

  /**
    * Deleting a user should delete the following
    * 1. User created posts
    * 2. User created comments
    *
    * The following are captured implicitly by detach deleting user nodes
    * 1. User liked posts
    * 2. User following others
    * 3. Other users following User
    */
  def deleteUserSocialMediaPosts(userId: UUID): Future[Unit] =
    for {
      _ <- socialMediaRepository.deleteAllCommentsForUser(userId)
      postIds <- socialMediaRepository.getAllPostIdsForUser(userId)
      _ <-
        Future.sequence(postIds.map(postId => socialMediaRepository.deleteAllCommentsForPost(UUID.fromString(postId))))
      _ <- socialMediaRepository.deleteAllPostsForUser(userId)
    } yield ()

  def createPostForUser(post: Post.Create): Future[Post] =
    socialMediaRepository.createUserPost(post)

  def getPostsByUser(
    userId: UUID,
    requestingUserId: UUID,
    createdBefore: Long,
    limit: Int
  ): Future[Either[DomainError, Seq[Post]]] =
    (for {
      _ <- EitherT[Future, DomainError, Boolean] {
        if (requestingUserId == userId) Future.successful(Right(true))
        else
          userRelationshipsRepository
            .getUserIfFollowingOtherUser(requestingUserId, userId)
            .map(_.map(_ => Right(true)).getOrElse(Left(EntityNotAccessible("User not following other user!"))))
      }
      posts <- EitherT.right[DomainError](socialMediaRepository.getPostsForUser(userId, createdBefore, limit))
    } yield posts).value

  /**
    * Returns posts belong to both current user as well posts of users being followed
    */
  def getNewsfeedPostsForUser(userId: UUID, createdBefore: Long, limit: Int): Future[Seq[Post]] =
    socialMediaRepository.getNewsfeedPostsForCurrentUser(userId, createdBefore, limit)

  def likePostForUser(postId: UUID, userId: UUID): Future[Either[DomainError, Unit]] =
    (for {
      _ <- EitherT[Future, DomainError, Unit](
        socialMediaRepository
          .getUserIfLikedPost(userId, postId)
          .map(_.map(_ => Left(EntityConflictError("User has already liked post!"))).getOrElse(Right()))
      )
      _ <- EitherT.right[DomainError](socialMediaRepository.makeUserLikePost(userId, postId))
      post <- EitherT[Future, DomainError, Post](
        socialMediaRepository
          .getPostById(postId)
          .map(_.map(Right.apply).getOrElse(Left(EntityNotFoundError("Post not found!"))))
      )
      _ <- EitherT.right[DomainError] {
        if (post.userId != userId) messageBusService.publishUserLikedPostNotification(userId, post.userId, postId)
        else Future.unit
      }
    } yield ()).value

  def unlikePostForUser(postId: UUID, userId: UUID): Future[Either[DomainError, Unit]] =
    (for {
      _ <- EitherT[Future, DomainError, PublicUserProfile](
        socialMediaRepository
          .getUserIfLikedPost(userId, postId)
          .map(_.map(Right.apply).getOrElse(Left(EntityNotFoundError("User has not liked post yet!"))))
      )
      _ <- EitherT.right[DomainError](socialMediaRepository.makeUserUnlikePost(userId, postId))
    } yield ()).value

  def getUsersWhoLikedPost(postId: UUID): Future[Seq[PublicUserProfile]] =
    socialMediaRepository.getUsersWhoLikedPost(postId)

  def getUsersWhoLikedPosts(postIds: Seq[UUID]): Future[Seq[UsersWhoLikedPost]] =
    Future.sequence {
      postIds.map { postId =>
        socialMediaRepository
          .getUsersWhoLikedPost(postId)
          .map(_.map(_.userId))
          .map(userIds => UsersWhoLikedPost(postId, userIds))
      }
    }

  def addCommentToPost(comment: PostComment.Create): Future[Either[DomainError, PostComment]] =
    (for {
      comment <- EitherT.right[DomainError](socialMediaRepository.addCommentToPost(comment))
      post <- EitherT[Future, DomainError, Post](
        socialMediaRepository
          .getPostById(comment.postId)
          .map(_.map(Right.apply).getOrElse(Left(EntityNotFoundError("Post not found!"))))
      )
      // Generate notifications for every distinct user who has participated in the conversation
      postComments <- EitherT.right[DomainError](socialMediaRepository.getCommentsForPost(post.postId))
      // commentParticipants defined as OP + distinct(commenters)
      commentParticipants = (postComments.map(_.userId) :+ post.userId).distinct
      notificationTargets = commentParticipants.filterNot(_ == comment.userId)
      _ <- EitherT.right[DomainError] {
        Future.sequence(
          notificationTargets.map(
            targetUserId =>
              messageBusService
                .publishUserCommentedOnPostNotification(comment.userId, targetUserId, comment.postId, post.userId)
          )
        )
      }
    } yield comment).value

  def getCommentsForPost(postId: UUID): Future[Seq[PostComment]] =
    socialMediaRepository.getCommentsForPost(postId)

  def getPostById(postId: UUID): Future[Either[DomainError, Post]] =
    socialMediaRepository
      .getPostById(postId)
      .map(_.map(Right.apply).getOrElse(Left(EntityNotFoundError("Post not found!"))))

}
