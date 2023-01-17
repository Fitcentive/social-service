package io.fitcentive.social.domain

import play.api.libs.json.{Json, Reads, Writes}

import java.util.UUID

case class UserFollowStatus(
  currentUserId: UUID,
  otherUserId: UUID,
  isCurrentUserFriendsWithOtherUser: Boolean,
  hasCurrentUserRequestedToFriendOtherUser: Boolean,
  hasOtherUserRequestedToFriendCurrentUser: Boolean,
)

object UserFollowStatus {
  implicit lazy val writes: Writes[UserFollowStatus] = Json.writes[UserFollowStatus]
  implicit lazy val reads: Reads[UserFollowStatus] = Json.reads[UserFollowStatus]
}
