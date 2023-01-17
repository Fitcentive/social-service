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

  def unfriendUser(currentUserId: UUID, targetUserId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        userRelationshipsApi
          .unfriendUser(currentUserId, targetUserId)
          .map(_ => Ok)
          .recover(resultErrorAsyncHandler)
      }(userRequest, currentUserId)
    }

  def getUserFriends(implicit userId: UUID, skip: Int = 0, limit: Int = 50): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        userRelationshipsApi
          .getUserFriends(userId, skip, limit)
          .map(users => Ok(Json.toJson(users)))
          .recover(resultErrorAsyncHandler)
      }
    }

  def requestToFriendUser(currentUserId: UUID, targetUserId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        userRelationshipsApi
          .requestToFriendUser(currentUserId, targetUserId)
          .map(handleEitherResult(_)(_ => Accepted))
          .recover(resultErrorAsyncHandler)
      }(userRequest, currentUserId)
    }

  def applyUserFriendRequestDecision(targetUserId: UUID, requestingUserId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit userRequest =>
      rejectIfNotEntitled {
        validateJson[UserFollowRequestDecisionPayload](userRequest.request.body.asJson) { decision =>
          userRelationshipsApi
            .applyUserFriendRequestDecision(targetUserId, requestingUserId, decision.isRequestApproved)
            .map(handleEitherResult(_)(_ => Ok))
            .recover(resultErrorAsyncHandler)
        }
      }(userRequest, targetUserId)
    }

  def getUserFriendStatus(implicit currentUserId: UUID, targetUserId: UUID): Action[AnyContent] =
    userAuthAction.async { implicit request =>
      rejectIfNotEntitled {
        userRelationshipsApi
          .getUserFriendStatus(currentUserId, targetUserId)
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

  def deleteUser(userId: UUID): Action[AnyContent] =
    internalAuthAction.async { implicit request =>
      userRelationshipsApi
        .deleteUser(userId)
        .map(_ => NoContent)
        .recover(resultErrorAsyncHandler)
    }

}
