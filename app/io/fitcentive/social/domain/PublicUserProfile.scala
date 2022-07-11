package io.fitcentive.social.domain

import play.api.libs.json.{Json, Reads, Writes}

import java.time.LocalDate
import java.util.UUID

case class PublicUserProfile(
  userId: UUID,
  username: Option[String],
  firstName: Option[String],
  lastName: Option[String],
  photoUrl: Option[String],
  dateOfBirth: Option[LocalDate]
)

object PublicUserProfile {
  implicit lazy val reads: Reads[PublicUserProfile] = Json.reads[PublicUserProfile]
  implicit lazy val writes: Writes[PublicUserProfile] = Json.writes[PublicUserProfile]
}
