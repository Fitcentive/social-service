package io.fitcentive.social.domain.payloads

import play.api.libs.json.{Json, Reads, Writes}

case class UserFollowRequestDecisionPayload(isRequestApproved: Boolean)

object UserFollowRequestDecisionPayload {
  implicit lazy val reads: Reads[UserFollowRequestDecisionPayload] = Json.reads[UserFollowRequestDecisionPayload]
  implicit lazy val writes: Writes[UserFollowRequestDecisionPayload] = Json.writes[UserFollowRequestDecisionPayload]
}
