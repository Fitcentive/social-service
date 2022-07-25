package io.fitcentive.social.infrastructure.database.graph

import io.fitcentive.social.domain.PublicUserProfile
import io.fitcentive.social.domain.location.Coordinates
import io.fitcentive.social.domain.types.CustomTypes.GraphDb
import io.fitcentive.social.infrastructure.contexts.Neo4jExecutionContext
import io.fitcentive.social.repositories.UserRelationshipsRepository
import neotypes.implicits.syntax.string._
import neotypes.generic.auto._
import neotypes.implicits.syntax.cypher._
import neotypes.DeferredQueryBuilder
import org.neo4j.driver.types.Point

import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.Future

class NeoTypesUserRelationshipsRepository @Inject() (val db: GraphDb)(implicit val ec: Neo4jExecutionContext)
  extends UserRelationshipsRepository {

  import NeoTypesUserRelationshipsRepository._

  override def removeFollowerForUser(requestingUserId: UUID, targetUserId: UUID): Future[Unit] =
    CYPHER_REMOVE_FOLLOWER_FOR_USER(requestingUserId, targetUserId)
      .readOnlyQuery[Unit]
      .single(db)

  override def makeUserUnFollowOther(requestingUserId: UUID, targetUserId: UUID): Future[Unit] =
    CYPHER_MAKE_USER_UNFOLLOW_OTHER(requestingUserId, targetUserId)
      .readOnlyQuery[Unit]
      .single(db)

  override def makeUserFollowOther(requestingUserId: UUID, targetUserId: UUID): Future[Unit] =
    CYPHER_MAKE_USER_FOLLOW_OTHER(requestingUserId, targetUserId)
      .readOnlyQuery[Unit]
      .single(db)

  override def upsertUser(user: PublicUserProfile): Future[PublicUserProfile] =
    CYPHER_UPSERT_USER_INFO(user)
      .readOnlyQuery[PublicNeo4jUserProfile]
      .single(db)
      .map(_.toPublicUserProfile)

  override def getUserFollowers(userId: UUID): Future[Seq[PublicUserProfile]] =
    CYPHER_GET_USER_FOLLOWERS(userId)
      .readOnlyQuery[PublicNeo4jUserProfile]
      .list(db)
      .map(_.map(_.toPublicUserProfile))

  override def getUserFollowing(userId: UUID): Future[Seq[PublicUserProfile]] =
    CYPHER_GET_USER_FOLLOWING(userId)
      .readOnlyQuery[PublicNeo4jUserProfile]
      .list(db)
      .map(_.map(_.toPublicUserProfile))

  override def getUserIfFollowingOtherUser(currentUser: UUID, otherUser: UUID): Future[Option[PublicUserProfile]] =
    CYPHER_GET_USER_IF_FOLLOWING_OTHER_USER(currentUser, otherUser)
      .readOnlyQuery[Option[PublicNeo4jUserProfile]]
      .single(db)
      .map(_.map(_.toPublicUserProfile))
}

object NeoTypesUserRelationshipsRepository {

  case class PublicNeo4jUserProfile(
    userId: UUID,
    username: Option[String],
    firstName: Option[String],
    lastName: Option[String],
    photoUrl: Option[String],
    dateOfBirth: Option[LocalDate],
    locationCenter: Option[Point],
    locationRadius: Option[Int]
  ) {
    def toPublicUserProfile: PublicUserProfile =
      PublicUserProfile(
        userId = userId,
        username = username,
        firstName = firstName,
        lastName = lastName,
        photoUrl = photoUrl,
        dateOfBirth = dateOfBirth,
        locationCenter = locationCenter.map(c => Coordinates(latitude = c.y, longitude = c.x)),
        locationRadius = locationRadius
      )
  }

  private def CYPHER_UPSERT_USER_INFO(user: PublicUserProfile): DeferredQueryBuilder =
    c"""
      MERGE (user: User { userId: ${user.userId} } )
      SET
        user.username = ${user.username},
        user.firstName = ${user.firstName},
        user.lastName = ${user.lastName},
        user.photoUrl = ${user.photoUrl},
        user.dateOfBirth = ${user.dateOfBirth},
        user.locationRadius = ${user.locationRadius},
        user.locationCenter =  ${user.locationCenter.map(_.toNeo4jPoint)}

      RETURN user"""

  private def CYPHER_MAKE_USER_FOLLOW_OTHER(requestingUserId: UUID, targetUserId: UUID): DeferredQueryBuilder =
    c"""
       MATCH (u1: User { userId: $requestingUserId } )
       MATCH (u2: User { userId: $targetUserId })
       MERGE (u1)-[r:IS_FOLLOWING]->(u2)
       RETURN u2"""

  private def CYPHER_GET_USER_FOLLOWERS(currentUserId: UUID): DeferredQueryBuilder =
    c"""
       MATCH (u1: User)-[r:IS_FOLLOWING]->(u2: User { userId: $currentUserId})
       RETURN u1"""

  private def CYPHER_GET_USER_FOLLOWING(currentUserId: UUID): DeferredQueryBuilder =
    c"""
       MATCH (u1: User { userId: $currentUserId} )-[r:IS_FOLLOWING]->(u2: User)
       RETURN u2"""

  private def CYPHER_MAKE_USER_UNFOLLOW_OTHER(requestingUserId: UUID, targetUserId: UUID): DeferredQueryBuilder =
    c"""
       MATCH (u1: User { userId: $requestingUserId })-[r:IS_FOLLOWING]->(u2: User { userId: $targetUserId })
       DELETE r"""

  private def CYPHER_REMOVE_FOLLOWER_FOR_USER(currentUserId: UUID, followingUserId: UUID): DeferredQueryBuilder =
    c"""
       MATCH (u1: User { userId: $followingUserId })-[r:IS_FOLLOWING]->(u2: User { userId: $currentUserId })
       DELETE r"""

  private def CYPHER_GET_USER_IF_FOLLOWING_OTHER_USER(currentUserId: UUID, otherUserId: UUID): DeferredQueryBuilder =
    c"""
       OPTIONAL MATCH (u1: User { userId: $currentUserId })-[r:IS_FOLLOWING]->(u2: User { userId: $otherUserId })
       RETURN u1"""

}
