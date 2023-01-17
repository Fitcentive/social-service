package io.fitcentive.social.infrastructure.rest

import io.fitcentive.sdk.config.ServerConfig
import io.fitcentive.sdk.error.DomainError
import io.fitcentive.social.domain.errors.UserServiceError
import io.fitcentive.social.domain.{User, UserFollowRequest}
import io.fitcentive.social.infrastructure.utils.ServiceSecretSupport
import io.fitcentive.social.services.{SettingsService, UserService}
import play.api.http.Status
import play.api.libs.ws.{EmptyBody, WSClient}

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RestUserService @Inject() (wsClient: WSClient, settingsService: SettingsService)(implicit ec: ExecutionContext)
  extends UserService
  with ServiceSecretSupport {

  val userServiceConfig: ServerConfig = settingsService.userServiceConfig
  val baseUrl: String = userServiceConfig.serverUrl

  override def requestToFriendUser(currentUserId: UUID, targetUserId: UUID): Future[Either[DomainError, Unit]] =
    wsClient
      .url(s"$baseUrl/api/internal/user/$currentUserId/friend-request/$targetUserId")
      .addHttpHeaders("Content-Type" -> "application/json")
      .addServiceSecret(settingsService)
      .post(EmptyBody)
      .map { response =>
        response.status match {
          case Status.ACCEPTED => Right(())
          case status          => Left(UserServiceError(s"Bad response from user service: $status"))
        }
      }

  override def getUserFriendRequest(requestingUserId: UUID, targetUserId: UUID): Future[Option[UserFollowRequest]] =
    wsClient
      .url(s"$baseUrl/api/internal/user/$requestingUserId/friend-request/$targetUserId")
      .addHttpHeaders("Content-Type" -> "application/json")
      .addServiceSecret(settingsService)
      .get()
      .map { response =>
        response.status match {
          case Status.OK => Some(response.json.as[UserFollowRequest])
          case _         => None
        }
      }

  override def deleteUserFriendRequest(requestingUserId: UUID, targetUserId: UUID): Future[Either[DomainError, Unit]] =
    wsClient
      .url(s"$baseUrl/api/internal/user/$requestingUserId/friend-request/$targetUserId")
      .addHttpHeaders("Content-Type" -> "application/json")
      .addServiceSecret(settingsService)
      .delete()
      .map { response =>
        response.status match {
          case Status.OK => Right(())
          case status    => Left(UserServiceError(s"Bad response from user service: $status"))
        }
      }

}
