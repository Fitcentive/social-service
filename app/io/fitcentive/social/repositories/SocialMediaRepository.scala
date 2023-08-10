package io.fitcentive.social.repositories

import com.google.inject.ImplementedBy
import io.fitcentive.social.domain.{Post, PostComment, PublicUserProfile}
import io.fitcentive.social.infrastructure.database.graph.NeoTypesSocialMediaRepository

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[NeoTypesSocialMediaRepository])
trait SocialMediaRepository {
  def deleteAllCommentsForUser(userId: UUID): Future[Unit]
  def deleteAllPostsForUser(userId: UUID): Future[Unit]
  def deleteUserPost(userId: UUID, postId: UUID): Future[Unit]
  def deleteAllCommentsForPost(postId: UUID): Future[Unit]
  def createUserPost(post: Post.Create): Future[Post]
  def getPostsForUser(userId: UUID, createdBefore: Long, limit: Int): Future[Seq[Post]]
  def getAllPostIdsForUser(userId: UUID): Future[Seq[String]]
  def getNewsfeedPostsForCurrentUser(userId: UUID, createdBefore: Long, limit: Int): Future[Seq[Post]]
  def getUserIfLikedPost(userId: UUID, postId: UUID): Future[Option[PublicUserProfile]]
  def makeUserLikePost(userId: UUID, postId: UUID): Future[Unit]
  def makeUserUnlikePost(userId: UUID, postId: UUID): Future[Unit]
  def getUsersWhoLikedPost(postId: UUID): Future[Seq[PublicUserProfile]]
  def addCommentToPost(comment: PostComment.Create): Future[PostComment]
  def getAllCommentsForPost(postId: UUID): Future[Seq[PostComment]]
  def getCommentsForPostInDescCreatedAt(postId: UUID, skip: Int, limit: Int): Future[Seq[PostComment]]
  def getMostRecentSpecifiedCommentsForPost(postId: UUID, mostRecentLimit: Int): Future[Seq[PostComment]]
  def getPostById(postId: UUID): Future[Option[Post]]
}
