package io.fitcentive.social.repositories

import com.google.inject.ImplementedBy
import io.fitcentive.social.domain.PublicUserProfile
import io.fitcentive.social.infrastructure.database.graph.NeoTypesUserRelationshipsRepository

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[NeoTypesUserRelationshipsRepository])
trait UserRelationshipsRepository {
  def deleteUser(userId: UUID): Future[Unit]
  def upsertUser(user: PublicUserProfile): Future[PublicUserProfile]
  def getUserIfFriendsWithOtherUser(currentUser: UUID, otherUser: UUID): Future[Option[PublicUserProfile]]
  def getUserFriends(userId: UUID, skip: Int, limit: Int): Future[Seq[PublicUserProfile]]
  def searchUserFriends(userId: UUID, searchQuery: String, skip: Int, limit: Int): Future[Seq[PublicUserProfile]]
  def makeUserFriendsWithOther(requestingUserId: UUID, targetUserId: UUID): Future[Unit]
  def makeUserUnfriendOther(requestingUserId: UUID, targetUserId: UUID): Future[Unit]
}
