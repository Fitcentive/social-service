package io.fitcentive.social.domain.payloads

import play.api.libs.json.{Json, Reads, Writes}

case class CreateCommentPayload(text: String)

object CreateCommentPayload {
  implicit lazy val reads: Reads[CreateCommentPayload] = Json.reads[CreateCommentPayload]
  implicit lazy val writes: Writes[CreateCommentPayload] = Json.writes[CreateCommentPayload]
}
