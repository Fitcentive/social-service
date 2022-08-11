package io.fitcentive.social.controllers

import io.fitcentive.sdk.play.{InternalAuthAction, UserAuthAction}
import io.fitcentive.sdk.utils.PlayControllerOps
import io.fitcentive.social.api.{SocialMediaApi, UserRelationshipsApi}
import io.fitcentive.social.domain.PublicUserProfile
import io.fitcentive.social.domain.payloads.UserFollowRequestDecisionPayload
import io.fitcentive.social.infrastructure.utils.ServerErrorHandler
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class UserRelationshipsController @Inject() (
  userRelationshipsApi: UserRelationshipsApi,
  userAuthAction: UserAuthAction,
  internalAuthAction: InternalAuthAction,
  cc: ControllerComponents
)(implicit exec: ExecutionContext)
  extends AbstractController(cc)
  with PlayControllerOps
  with ServerErrorHandler {

  def unfollowUser(currentUserId: UUID, targetUserId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        userRelationshipsApi
          .unfollowUser(currentUserId, targetUserId)
          .map(_ => Ok)
          .recover(resultErrorAsyncHandler)
      }(userRequest, currentUserId)
    }

  def removeFollower(currentUserId: UUID, followingUserId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        userRelationshipsApi
          .removeFollowerForUser(currentUserId, followingUserId)
          .map(_ => Ok)
          .recover(resultErrorAsyncHandler)
      }(userRequest, currentUserId)
    }

  def getUserFollowers(implicit userId: UUID, skip: Int = 0, limit: Int = 50): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        userRelationshipsApi
          .getUserFollowers(userId, skip, limit)
          .map(users => Ok(Json.toJson(users)))
          .recover(resultErrorAsyncHandler)
      }
    }

  def getUserFollowing(implicit userId: UUID, skip: Int = 0, limit: Int = 50): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        userRelationshipsApi
          .getUserFollowing(userId, skip, limit)
          .map(users => Ok(Json.toJson(users)))
          .recover(resultErrorAsyncHandler)
      }
    }

  def requestToFollowUser(currentUserId: UUID, targetUserId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        userRelationshipsApi
          .requestToFollowUser(currentUserId, targetUserId)
          .map(handleEitherResult(_)(_ => Accepted))
          .recover(resultErrorAsyncHandler)
      }(userRequest, currentUserId)
    }

  def applyUserFollowRequestDecision(targetUserId: UUID, requestingUserId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        validateJson[UserFollowRequestDecisionPayload](userRequest.request.body.asJson) { decision =>
          userRelationshipsApi
            .applyUserFollowRequestDecision(targetUserId, requestingUserId, decision.isRequestApproved)
            .map(handleEitherResult(_)(_ => Ok))
            .recover(resultErrorAsyncHandler)
        }
      }(userRequest, targetUserId)
    }

  def getUserFollowStatus(implicit currentUserId: UUID, targetUserId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        userRelationshipsApi
          .getUserFollowStatus(currentUserId, targetUserId)
          .map(users => Ok(Json.toJson(users)))
          .recover(resultErrorAsyncHandler)
      }(request, currentUserId)
    }

  //-----------------------
  // Internal routes
  //-----------------------

  def upsertUser: Action[AnyContent] =
    internalAuthAction.async { implicit request =>
      validateJson[PublicUserProfile](request.body.asJson) { publicUser =>
        userRelationshipsApi
          .upsertUser(publicUser)
          .map(user => Ok(Json.toJson(user)))
          .recover(resultErrorAsyncHandler)
      }
    }

}
