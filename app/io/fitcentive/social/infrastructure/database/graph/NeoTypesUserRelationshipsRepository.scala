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

  override def makeUserUnfriendOther(requestingUserId: UUID, targetUserId: UUID): Future[Unit] =
    CYPHER_MAKE_USER_UNFRIEND_OTHER(requestingUserId, targetUserId)
      .query[Unit]
      .single(db)

  override def makeUserFriendsWithOther(requestingUserId: UUID, targetUserId: UUID): Future[Unit] =
    CYPHER_MAKE_USER_FRIENDS_WITH_OTHER(requestingUserId, targetUserId)
      .query[Unit]
      .single(db)

  override def deleteUser(userId: UUID): Future[Unit] =
    CYPHER_DELETE_USER(userId)
      .query[Unit]
      .single(db)

  override def upsertUser(user: PublicUserProfile): Future[PublicUserProfile] =
    CYPHER_UPSERT_USER_INFO(user)
      .query[PublicNeo4jUserProfile]
      .single(db)
      .map(_.toPublicUserProfile)

  override def getUserFriends(userId: UUID, skip: Int, limit: Int): Future[Seq[PublicUserProfile]] =
    CYPHER_GET_USER_FRIENDS(userId, skip, limit)
      .readOnlyQuery[PublicNeo4jUserProfile]
      .list(db)
      .map(_.map(_.toPublicUserProfile))

  override def searchUserFriends(
    userId: UUID,
    searchQuery: String,
    skip: Int,
    limit: Int
  ): Future[Seq[PublicUserProfile]] =
    CYPHER_SEARCH_USER_FRIENDS(userId, searchQuery, skip, limit)
      .readOnlyQuery[PublicNeo4jUserProfile]
      .list(db)
      .map(_.map(_.toPublicUserProfile))

  override def getUserIfFriendsWithOtherUser(currentUser: UUID, otherUser: UUID): Future[Option[PublicUserProfile]] =
    CYPHER_GET_USER_IF_FRIENDS_WITH_OTHER_USER(currentUser, otherUser)
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
    locationRadius: Option[Int],
    gender: Option[String],
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
        locationRadius = locationRadius,
        gender = gender,
      )
  }

  private def CYPHER_DELETE_USER(userId: UUID): DeferredQueryBuilder =
    c"""
     OPTIONAL MATCH (user: User { userId: $userId })
     DETACH DELETE user
     """

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
        user.locationCenter =  ${user.locationCenter.map(_.toNeo4jPoint)},
        user.gender = ${user.gender}

      RETURN user"""

  private def CYPHER_MAKE_USER_FRIENDS_WITH_OTHER(requestingUserId: UUID, targetUserId: UUID): DeferredQueryBuilder =
    c"""
       MATCH (u1: User { userId: $requestingUserId } )
       MATCH (u2: User { userId: $targetUserId })
       MERGE (u1)-[r:IS_FRIENDS_WITH]-(u2)
       RETURN u2"""

  private def CYPHER_GET_USER_FRIENDS(currentUserId: UUID, skip: Int, limit: Int): DeferredQueryBuilder =
    c"""
       MATCH (u1: User)-[r:IS_FRIENDS_WITH]-(u2: User { userId: $currentUserId})
       RETURN u1
       SKIP $skip
       LIMIT $limit"""

  private def CYPHER_SEARCH_USER_FRIENDS(
    currentUserId: UUID,
    searchQuery: String,
    skip: Int,
    limit: Int
  ): DeferredQueryBuilder =
    c"""
       MATCH (u1: User)-[r:IS_FRIENDS_WITH]-(u2: User { userId: $currentUserId})
       WHERE (
        toLower(u1.firstName + ' ' + u1.lastName) CONTAINS toLower($searchQuery) 
        OR toLower(u1.username) CONTAINS toLower($searchQuery)
       )
       RETURN u1
       SKIP $skip
       LIMIT $limit"""

  private def CYPHER_MAKE_USER_UNFRIEND_OTHER(requestingUserId: UUID, targetUserId: UUID): DeferredQueryBuilder =
    c"""
       MATCH (u1: User { userId: $requestingUserId })-[r:IS_FRIENDS_WITH]-(u2: User { userId: $targetUserId })
       DELETE r"""

  private def CYPHER_GET_USER_IF_FRIENDS_WITH_OTHER_USER(currentUserId: UUID, otherUserId: UUID): DeferredQueryBuilder =
    c"""
       OPTIONAL MATCH (u1: User { userId: $currentUserId })-[r:IS_FRIENDS_WITH]-(u2: User { userId: $otherUserId })
       RETURN u1"""

}
