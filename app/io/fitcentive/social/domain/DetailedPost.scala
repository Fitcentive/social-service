package io.fitcentive.social.domain

import play.api.libs.json.{Json, Reads, Writes}

import java.util.UUID

case class DetailedPost(post: Post, likedUserIds: Seq[UUID], mostRecentComments: Seq[PostComment])

object DetailedPost {
  implicit lazy val reads: Reads[DetailedPost] = Json.reads[DetailedPost]
  implicit lazy val writes: Writes[DetailedPost] = Json.writes[DetailedPost]
}
