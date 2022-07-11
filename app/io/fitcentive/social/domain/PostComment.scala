package io.fitcentive.social.domain

import play.api.libs.json.{Json, Reads, Writes}

import java.time.LocalDateTime
import java.util.UUID
import scala.util.chaining.scalaUtilChainingOps

case class PostComment(
  postId: UUID,
  commentId: UUID,
  userId: UUID,
  text: String,
  createdAt: LocalDateTime,
  updatedAt: LocalDateTime
)

object PostComment {
  implicit lazy val reads: Reads[PostComment] = Json.reads[PostComment]
  implicit lazy val writes: Writes[PostComment] = Json.writes[PostComment]

  case class Create(postId: UUID, userId: UUID, text: String) {
    def toNewInsertObject: Insert =
      LocalDateTime.now.pipe { now =>
        Insert(
          postId = postId,
          userId = userId,
          commentId = UUID.randomUUID(),
          text = text,
          createdAt = now,
          updatedAt = now
        )
      }
  }

  object Create {
    implicit lazy val reads: Reads[PostComment.Create] = Json.reads[PostComment.Create]
    implicit lazy val writes: Writes[PostComment.Create] = Json.writes[PostComment.Create]
  }

  case class Insert(
    postId: UUID,
    userId: UUID,
    commentId: UUID,
    text: String,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime
  )

  object Insert {
    implicit lazy val reads: Reads[PostComment.Insert] = Json.reads[PostComment.Insert]
    implicit lazy val writes: Writes[PostComment.Insert] = Json.writes[PostComment.Insert]
  }
}
