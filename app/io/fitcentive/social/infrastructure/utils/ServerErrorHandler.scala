package io.fitcentive.social.infrastructure.utils

import io.fitcentive.sdk.error.{DomainError, EntityConflictError, EntityNotAccessible, EntityNotFoundError}
import io.fitcentive.sdk.logging.AppLogger
import io.fitcentive.sdk.utils.DomainErrorHandler
import io.fitcentive.social.domain.errors.UserServiceError
import play.api.mvc.Result
import play.api.mvc.Results._

trait ServerErrorHandler extends DomainErrorHandler with AppLogger {

  override def resultErrorAsyncHandler: PartialFunction[Throwable, Result] = {
    case e: Exception =>
      logError(s"${e.getMessage}", e)
      InternalServerError(e.getMessage)
  }

  override def domainErrorHandler: PartialFunction[DomainError, Result] = {
    case EntityNotFoundError(reason) => NotFound(reason)
    case EntityConflictError(reason) => Conflict(reason)
    case EntityNotAccessible(reason) => Forbidden(reason)
    case UserServiceError(reason)    => BadRequest(reason)
    case _                           => InternalServerError("Unexpected error occurred ")
  }

}
