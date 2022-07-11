package io.fitcentive.social.domain.payloads

import play.api.libs.json.{Json, Reads, Writes}

import java.util.UUID

case class GetUsersWhoLikedPostsPayload(postIds: Seq[UUID])

object GetUsersWhoLikedPostsPayload {
  implicit lazy val reads: Reads[GetUsersWhoLikedPostsPayload] = Json.reads[GetUsersWhoLikedPostsPayload]
  implicit lazy val writes: Writes[GetUsersWhoLikedPostsPayload] = Json.writes[GetUsersWhoLikedPostsPayload]
}
