package io.fitcentive.social.infrastructure.database.graph

import io.fitcentive.social.domain.{Post, PostComment, PublicUserProfile}
import io.fitcentive.social.domain.types.CustomTypes.GraphDb
import io.fitcentive.social.infrastructure.contexts.Neo4jExecutionContext
import io.fitcentive.social.infrastructure.database.graph.NeoTypesUserRelationshipsRepository.PublicNeo4jUserProfile
import io.fitcentive.social.repositories.SocialMediaRepository
import neotypes.DeferredQueryBuilder
import neotypes.implicits.syntax.string._
import neotypes.generic.auto._
import neotypes.implicits.syntax.cypher._

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.Future

class NeoTypesSocialMediaRepository @Inject() (val db: GraphDb)(implicit val ec: Neo4jExecutionContext)
  extends SocialMediaRepository {

  import NeoTypesSocialMediaRepository._

  override def getPostById(postId: UUID): Future[Option[Post]] =
    CYPHER_GET_POST_BY_ID(postId)
      .readOnlyQuery[Option[Post]]
      .single(db)

  override def getUserIfLikedPost(userId: UUID, postId: UUID): Future[Option[PublicUserProfile]] =
    CYPHER_GET_USER_IF_LIKED_POST(userId, postId)
      .readOnlyQuery[Option[PublicNeo4jUserProfile]]
      .single(db)
      .map(_.map(_.toPublicUserProfile))

  override def makeUserLikePost(userId: UUID, postId: UUID): Future[Unit] =
    CYPHER_MAKE_USER_LIKE_POST(userId, postId)
      .readOnlyQuery[Unit]
      .single(db)

  override def makeUserUnlikePost(userId: UUID, postId: UUID): Future[Unit] =
    CYPHER_MAKE_USER_UNLIKE_POST(userId, postId)
      .readOnlyQuery[Unit]
      .single(db)

  override def getUsersWhoLikedPost(postId: UUID): Future[Seq[PublicUserProfile]] =
    CYPHER_GET_USERS_WHO_LIKED_POST(postId)
      .readOnlyQuery[PublicNeo4jUserProfile]
      .list(db)
      .map(_.map(_.toPublicUserProfile))

  override def addCommentToPost(comment: PostComment.Create): Future[PostComment] =
    CYPHER_ADD_COMMENT_TO_POST(comment)
      .readOnlyQuery[PostComment]
      .single(db)

  override def getCommentsForPost(postId: UUID): Future[Seq[PostComment]] =
    CYPHER_GET_COMMENTS_FOR_POST(postId)
      .readOnlyQuery[PostComment]
      .list(db)

  override def getNewsfeedPostsForCurrentUser(userId: UUID): Future[Seq[Post]] =
    CYPHER_GET_USER_NEWSFEED_POSTS(userId)
      .readOnlyQuery[Post]
      .list(db)

  override def getPostsForUser(userId: UUID): Future[Seq[Post]] =
    CYPHER_GET_USER_POSTS(userId)
      .readOnlyQuery[Post]
      .list(db)

  override def createUserPost(post: Post.Create): Future[Post] =
    CYPHER_CREATE_USER_POST(post)
      .readOnlyQuery[Post]
      .single(db)
}

object NeoTypesSocialMediaRepository {
  private def CYPHER_GET_POST_BY_ID(postId: UUID): DeferredQueryBuilder =
    c"""
      OPTIONAL MATCH (post: Post { postId: $postId })
      WITH post
      OPTIONAL MATCH (u: User)-[rel:LIKED]->(post)
      WITH post, count(rel) AS numberOfLikes
      OPTIONAL MATCH (post)-[:HAS_COMMENT]->(c: Comment)
      WITH
        post.postId AS postId, post.userId AS userId, post.text AS text,
        numberOfLikes, count(c) AS numberOfComments,
        post.photoUrl AS photoUrl, post.createdAt AS createdAt, post.updatedAt AS updatedAt
      ORDER BY updatedAt DESC
      RETURN postId, userId, text, numberOfLikes, numberOfComments, photoUrl, createdAt, updatedAt"""

  private def CYPHER_GET_USER_IF_LIKED_POST(userId: UUID, postId: UUID): DeferredQueryBuilder = {
    c"""
     OPTIONAL MATCH (u: User { userId: $userId })-[rel:LIKED]->(p: Post { postId: $postId })
     RETURN u
     """
  }

  private def CYPHER_MAKE_USER_LIKE_POST(userId: UUID, postId: UUID): DeferredQueryBuilder = {
    c"""
     MATCH (user: User { userId: $userId })
     WITH user
     MATCH (post: Post { postId: $postId })
     WITH user, post
     MERGE (user)-[rel:LIKED]->(post)
     """
  }

  private def CYPHER_MAKE_USER_UNLIKE_POST(userId: UUID, postId: UUID): DeferredQueryBuilder = {
    c"""
     MATCH (user: User { userId: $userId })
     WITH user
     MATCH (post: Post { postId: $postId })
     WITH user, post
     MATCH (user)-[rel:LIKED]->(post)
     DELETE rel
     """
  }

  private def CYPHER_GET_USERS_WHO_LIKED_POST(postId: UUID): DeferredQueryBuilder = {
    c"""
     MATCH (u: User)-[rel:LIKED]->(p: Post { postId: $postId })
     RETURN u
     """
  }

  private def CYPHER_ADD_COMMENT_TO_POST(comment: PostComment.Create): DeferredQueryBuilder = {
    val commentInsert = comment.toNewInsertObject
    c"""
     MATCH (user: User { userId: ${comment.userId} })
     WITH user
     MATCH (post: Post { postId: ${comment.postId} })
     WITH user, post
     CREATE (comment: Comment { commentId: ${commentInsert.commentId} } )
     SET
        comment.postId = ${commentInsert.postId},
        comment.userId = ${commentInsert.userId},
        comment.text = ${commentInsert.text},
        comment.createdAt = ${commentInsert.createdAt},
        comment.updatedAt = ${commentInsert.updatedAt}
     WITH user, post, comment
     CREATE (post)-[:HAS_COMMENT]->(comment)
     WITH user, post, comment
     CREATE (user)-[:COMMENTED]->(comment)
     WITH comment.commentId AS commentId, comment.postId AS postId, comment.userId AS userId,
          comment.text AS text, comment.createdAt AS createdAt, comment.updatedAt AS updatedAt
     RETURN postId, commentId, userId, text, createdAt, updatedAt"""
  }

  private def CYPHER_GET_COMMENTS_FOR_POST(postId: UUID): DeferredQueryBuilder = {
    c"""
     MATCH (post: Post { postId: $postId })-[rel:HAS_COMMENT]->(comment: Comment)
     WITH comment
     ORDER BY comment.createdAt
     RETURN comment
     """
  }

  private def CYPHER_CREATE_USER_POST(post: Post.Create): DeferredQueryBuilder = {
    val postInsert = post.toNewInsertObject
    c"""
      MATCH (u: User { userId: ${post.userId} })
      WITH u
      CREATE (post: Post { postId: ${postInsert.postId} } )
      SET
        post.userId = ${postInsert.userId},
        post.text = ${postInsert.text},
        post.photoUrl = ${postInsert.photoUrl},
        post.createdAt = ${postInsert.createdAt},
        post.updatedAt = ${postInsert.updatedAt}
      WITH u, post
      CREATE (u)-[:POSTED]->(post)
      WITH post.postId AS postId, post.userId AS userId, post.text AS text,
           post.photoUrl AS photoUrl,  0 as numberOfLikes, 0 as numberOfComments,
           post.createdAt AS createdAt, post.updatedAt AS updatedAt
      RETURN postId, userId, text, photoUrl, numberOfLikes, numberOfComments, createdAt, updatedAt"""
  }

  private def CYPHER_GET_USER_POSTS(userId: UUID): DeferredQueryBuilder =
    c"""
      MATCH (currentUser: User { userId: $userId })-[:POSTED]->(post: Post)
      WITH post
      OPTIONAL MATCH (u: User)-[rel:LIKED]->(post)
      WITH post, count(rel) AS numberOfLikes
      OPTIONAL MATCH (post)-[:HAS_COMMENT]->(c: Comment)
      WITH
        post.postId AS postId, post.userId AS userId, post.text AS text,
        numberOfLikes, count(c) AS numberOfComments,
        post.photoUrl AS photoUrl, post.createdAt AS createdAt, post.updatedAt AS updatedAt
      ORDER BY updatedAt DESC
      RETURN postId, userId, text, numberOfLikes, numberOfComments, photoUrl, createdAt, updatedAt"""

  private def CYPHER_GET_USER_NEWSFEED_POSTS(currentUserId: UUID): DeferredQueryBuilder =
    c"""
      CALL {
        MATCH (current: User { userId: $currentUserId })-[:IS_FOLLOWING]->(friend: User)-[:POSTED]->(post: Post)
        WITH post
        OPTIONAL MATCH (u: User)-[rel:LIKED]->(post)
        WITH post, count(rel) AS numberOfLikes
        OPTIONAL MATCH (post)-[:HAS_COMMENT]->(c: Comment)
        WITH
          post.postId AS postId, post.userId AS userId, post.text AS text,
          numberOfLikes, count(c) AS numberOfComments,
          post.photoUrl AS photoUrl, post.createdAt AS createdAt, post.updatedAt AS updatedAt
        RETURN postId, userId, text, numberOfLikes, numberOfComments, photoUrl, createdAt, updatedAt

        UNION

        MATCH (currentUser: User { userId: $currentUserId })-[:POSTED]->(post: Post)
        WITH post
        OPTIONAL MATCH (u: User)-[rel:LIKED]->(post)
        WITH post, count(rel) AS numberOfLikes
        OPTIONAL MATCH (post)-[:HAS_COMMENT]->(c: Comment)
        WITH
          post.postId AS postId, post.userId AS userId, post.text AS text,
          numberOfLikes, count(c) AS numberOfComments,
          post.photoUrl AS photoUrl, post.createdAt AS createdAt, post.updatedAt AS updatedAt
        RETURN postId, userId, text, numberOfLikes, numberOfComments, photoUrl, createdAt, updatedAt
      }

      RETURN postId, userId, text, numberOfLikes, numberOfComments, photoUrl, createdAt, updatedAt
      ORDER BY updatedAt DESC"""

}
