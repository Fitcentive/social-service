package io.fitcentive.social.domain.responses

import play.api.libs.json.{Json, Reads, Writes}

import java.util.UUID

case class UsersWhoLikedPost(postId: UUID, userIds: Seq[UUID])

object UsersWhoLikedPost {
  implicit lazy val reads: Reads[UsersWhoLikedPost] = Json.reads[UsersWhoLikedPost]
  implicit lazy val writes: Writes[UsersWhoLikedPost] = Json.writes[UsersWhoLikedPost]
}
