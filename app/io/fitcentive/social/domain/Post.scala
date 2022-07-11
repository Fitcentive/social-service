package io.fitcentive.social.domain

import play.api.libs.json.{Json, Reads, Writes}

import java.time.LocalDateTime
import java.util.UUID
import scala.util.chaining.scalaUtilChainingOps

case class Post(
  postId: UUID,
  userId: UUID,
  text: String,
  photoUrl: Option[String],
  numberOfLikes: Int,
  numberOfComments: Int,
  createdAt: LocalDateTime,
  updatedAt: LocalDateTime
)

object Post {
  implicit lazy val reads: Reads[Post] = Json.reads[Post]
  implicit lazy val writes: Writes[Post] = Json.writes[Post]

  case class Insert(
    postId: UUID,
    userId: UUID,
    text: String,
    photoUrl: Option[String],
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime
  )

  case class Create(userId: UUID, text: String, photoUrl: Option[String]) {
    def toNewInsertObject: Insert =
      LocalDateTime.now.pipe { now =>
        Insert(
          postId = UUID.randomUUID(),
          userId = userId,
          text = text,
          photoUrl = photoUrl,
          createdAt = now,
          updatedAt = now
        )
      }
  }

  object Create {
    implicit lazy val reads: Reads[Post.Create] = Json.reads[Post.Create]
    implicit lazy val writes: Writes[Post.Create] = Json.writes[Post.Create]
  }
}
