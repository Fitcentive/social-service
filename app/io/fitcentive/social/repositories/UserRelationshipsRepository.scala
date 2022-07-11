package io.fitcentive.social.repositories

import com.google.inject.ImplementedBy
import io.fitcentive.social.domain.PublicUserProfile
import io.fitcentive.social.infrastructure.database.graph.NeoTypesUserRelationshipsRepository

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[NeoTypesUserRelationshipsRepository])
trait UserRelationshipsRepository {
  def upsertUser(user: PublicUserProfile): Future[PublicUserProfile]
  def getUserIfFollowingOtherUser(currentUser: UUID, otherUser: UUID): Future[Option[PublicUserProfile]]
  def getUserFollowers(userId: UUID): Future[Seq[PublicUserProfile]]
  def getUserFollowing(userId: UUID): Future[Seq[PublicUserProfile]]
  def makeUserFollowOther(requestingUserId: UUID, targetUserId: UUID): Future[Unit]
  def makeUserUnFollowOther(requestingUserId: UUID, targetUserId: UUID): Future[Unit]
  def removeFollowerForUser(currentUser: UUID, followingUser: UUID): Future[Unit]
}
