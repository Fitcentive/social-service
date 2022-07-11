package io.fitcentive.social.controllers

import io.fitcentive.sdk.play.{InternalAuthAction, UserAuthAction}
import io.fitcentive.sdk.utils.PlayControllerOps
import io.fitcentive.social.api.SocialMediaApi
import io.fitcentive.social.domain.{Post, PostComment}
import io.fitcentive.social.domain.payloads.{CreateCommentPayload, GetUsersWhoLikedPostsPayload}
import io.fitcentive.social.infrastructure.utils.ServerErrorHandler
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

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

  def getPostsForUser(implicit userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      socialMediaApi
        .getPostsByUser(userId, userRequest.authorizedUser.userId)
        .map(handleEitherResult(_)(posts => Ok(Json.toJson(posts))))
        .recover(resultErrorAsyncHandler)
    }

  def getNewsfeedForUser(implicit userId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      socialMediaApi
        .getNewsfeedPostsForUser(userId)
        .map(posts => Ok(Json.toJson(posts)))
        .recover(resultErrorAsyncHandler)
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

  def addCommentToPost(userId: UUID, postId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      validateJson[CreateCommentPayload](userRequest.request.body.asJson) { comment =>
        socialMediaApi
          .addCommentToPost(PostComment.Create(postId = postId, userId = userId, text = comment.text))
          .map(commentResponse => Ok(Json.toJson(commentResponse)))
          .recover(resultErrorAsyncHandler)
      }
    }

}
