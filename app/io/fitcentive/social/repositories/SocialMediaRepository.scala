package io.fitcentive.social.repositories

import com.google.inject.ImplementedBy
import io.fitcentive.social.domain.{Post, PostComment, PublicUserProfile}
import io.fitcentive.social.infrastructure.database.graph.NeoTypesSocialMediaRepository

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[NeoTypesSocialMediaRepository])
trait SocialMediaRepository {
  def createUserPost(post: Post.Create): Future[Post]
  def getPostsForUser(userId: UUID): Future[Seq[Post]]
  def getNewsfeedPostsForCurrentUser(userId: UUID): Future[Seq[Post]]
  def getUserIfLikedPost(userId: UUID, postId: UUID): Future[Option[PublicUserProfile]]
  def makeUserLikePost(userId: UUID, postId: UUID): Future[Unit]
  def makeUserUnlikePost(userId: UUID, postId: UUID): Future[Unit]
  def getUsersWhoLikedPost(postId: UUID): Future[Seq[PublicUserProfile]]
  def addCommentToPost(comment: PostComment.Create): Future[PostComment]
  def getCommentsForPost(postId: UUID): Future[Seq[PostComment]]
}
