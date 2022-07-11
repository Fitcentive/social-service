package io.fitcentive.social.domain

import play.api.libs.json.{Json, Reads, Writes}

import java.time.Instant
import java.util.UUID

case class UserFollowRequest(requestingUserId: UUID, targetUserId: UUID, createdAt: Instant)

object UserFollowRequest {
  implicit lazy val writes: Writes[UserFollowRequest] = Json.writes[UserFollowRequest]
  implicit lazy val reads: Reads[UserFollowRequest] = Json.reads[UserFollowRequest]
}
