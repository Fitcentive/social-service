package io.fitcentive.social.domain.errors

import io.fitcentive.sdk.error.DomainError

import java.util.UUID

case class UserServiceError(reason: String) extends DomainError {
  override def code: UUID = UUID.fromString("69366371-926a-4a92-acb1-d846275898c3")
}
