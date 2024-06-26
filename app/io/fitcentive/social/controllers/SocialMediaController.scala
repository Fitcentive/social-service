package io.fitcentive.social.controllers

import io.fitcentive.sdk.play.{InternalAuthAction, UserAuthAction}
import io.fitcentive.sdk.utils.PlayControllerOps
import io.fitcentive.social.api.SocialMediaApi
import io.fitcentive.social.domain.{Post, PostComment}
import io.fitcentive.social.domain.payloads.{CreateCommentPayload, GetUsersWhoLikedPostsPayload}
import io.fitcentive.social.infrastructure.utils.ServerErrorHandler
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class SocialMediaController @Inject() (
  socialMediaApi: SocialMediaApi,
  userAuthAction: UserAuthAction,
  internalAuthAction: InternalAuthAction,
  cc: ControllerComponents
)(implicit exec: ExecutionContext)
  extends AbstractController(cc)
  with PlayControllerOps
  with ServerErrorHandler {

  def deletePostForUser(implicit userId: UUID, postId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        socialMediaApi
          .deletePostForUser(userId, postId)
          .map(_ => NoContent)
          .recover(resultErrorAsyncHandler)
      }(userRequest, userId)
    }

  def createPostForUser(implicit userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        validateJson[Post.Create](userRequest.request.body.asJson) { userPostCreate =>
          socialMediaApi
            .createPostForUser(userPostCreate)
            .map(post => Ok(Json.toJson(post)))
            .recover(resultErrorAsyncHandler)
        }
      }
    }

  def getDetailedPostsForUser(implicit
    userId: UUID,
    createdBefore: Option[Long] = None,
    limit: Int = 10,
    commentPreviewLimit: Int = 3,
  ): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      socialMediaApi
        .getDetailedPostsByUser(
          userId,
          userRequest.authorizedUser.userId,
          createdBefore.fold(Instant.now.toEpochMilli)(identity),
          limit,
          commentPreviewLimit
        )
        .map(handleEitherResult(_)(posts => Ok(Json.toJson(posts))))
        .recover(resultErrorAsyncHandler)
    }

  def getPostsForUser(implicit userId: UUID, createdBefore: Option[Long] = None, limit: Int = 10): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      socialMediaApi
        .getPostsByUser(
          userId,
          userRequest.authorizedUser.userId,
          createdBefore.fold(Instant.now.toEpochMilli)(identity),
          limit
        )
        .map(handleEitherResult(_)(posts => Ok(Json.toJson(posts))))
        .recover(resultErrorAsyncHandler)
    }

  def getDetailedNewsfeedForUser(implicit
    userId: UUID,
    createdBefore: Option[Long] = None,
    limit: Int = 10,
    mostRecentCommentsLimit: Int = 3,
  ): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        socialMediaApi
          .getDetailedNewsFeedForUser(
            userId,
            createdBefore.fold(Instant.now.toEpochMilli)(identity),
            limit,
            mostRecentCommentsLimit
          )
          .map(posts => Ok(Json.toJson(posts)))
          .recover(resultErrorAsyncHandler)
      }
    }

  def getNewsfeedForUser(implicit
    userId: UUID,
    createdBefore: Option[Long] = None,
    limit: Int = 10
  ): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        socialMediaApi
          .getNewsfeedPostsForUser(userId, createdBefore.fold(Instant.now.toEpochMilli)(identity), limit)
          .map(posts => Ok(Json.toJson(posts)))
          .recover(resultErrorAsyncHandler)
      }
    }

  def likePostForUser(implicit userId: UUID, postId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        socialMediaApi
          .likePostForUser(postId, userId)
          .map(handleEitherResult(_)(_ => Ok))
          .recover(resultErrorAsyncHandler)
      }(userRequest, userId)
    }

  def unlikePostForUser(implicit userId: UUID, postId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        socialMediaApi
          .unlikePostForUser(postId, userId)
          .map(handleEitherResult(_)(_ => Ok))
          .recover(resultErrorAsyncHandler)
      }(userRequest, userId)
    }

  def getUsersWhoLikedPost(postId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      socialMediaApi
        .getUsersWhoLikedPost(postId)
        .map(users => Ok(Json.toJson(users)))
        .recover(resultErrorAsyncHandler)
    }

  def getUserIdsWhoLikedPosts: Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      validateJson[GetUsersWhoLikedPostsPayload](userRequest.request.body.asJson) { payload =>
        socialMediaApi
          .getUsersWhoLikedPosts(payload.postIds)
          .map(users => Ok(Json.toJson(users)))
          .recover(resultErrorAsyncHandler)
      }
    }

  def getCommentsForPost(postId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      socialMediaApi
        .getCommentsForPost(postId)
        .map(users => Ok(Json.toJson(users)))
        .recover(resultErrorAsyncHandler)
    }

  def getCommentsForPostInDescCreatedAt(
    postId: UUID,
    skip: Option[Int] = None,
    limit: Option[Int] = None
  ): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      socialMediaApi
        .getCommentsForPostInDescCreatedAt(postId, skip, limit)
        .map(users => Ok(Json.toJson(users)))
        .recover(resultErrorAsyncHandler)
    }

  def getPostById(postId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      socialMediaApi
        .getPostById(postId)
        .map(handleEitherResult(_)(post => Ok(Json.toJson(post))))
        .recover(resultErrorAsyncHandler)
    }

  def addCommentToPost(userId: UUID, postId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      validateJson[CreateCommentPayload](userRequest.request.body.asJson) { comment =>
        socialMediaApi
          .addCommentToPost(PostComment.Create(postId = postId, userId = userId, text = comment.text))
          .map(handleEitherResult(_)(commentResponse => Ok(Json.toJson(commentResponse))))
          .recover(resultErrorAsyncHandler)
      }
    }

  // ---------------------------------------------------------------------
  // Internal auth routes
  // ---------------------------------------------------------------------
  def deleteUserSocialMediaContent(userId: UUID): Action[AnyContent] =
    internalAuthAction.async { implicit request =>
      socialMediaApi
        .deleteUserSocialMediaPosts(userId)
        .map(_ => NoContent)
        .recover(resultErrorAsyncHandler)
    }
}
