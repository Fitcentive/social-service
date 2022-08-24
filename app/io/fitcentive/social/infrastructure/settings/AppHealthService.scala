package io.fitcentive.social.infrastructure.settings

import io.fitcentive.social.domain.types.CustomTypes.GraphDb
import io.fitcentive.social.infrastructure.contexts.Neo4jExecutionContext
import io.fitcentive.social.services.HealthService
import neotypes.DeferredQueryBuilder
import neotypes.implicits.syntax.cypher._

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class AppHealthService @Inject() (val graphDb: GraphDb)(implicit val ec: Neo4jExecutionContext) extends HealthService {

  import AppHealthService._

  override def isGraphDatabaseAvailable: Future[Boolean] =
    CYPHER_SELECT_1
      .readOnlyQuery[Int]
      .single(graphDb)
      .map(_ => true)(ec)

}

object AppHealthService {
  val CYPHER_SELECT_1: DeferredQueryBuilder =
    c"""
     OPTIONAL MATCH (n) RETURN 1 LIMIT 1
     """
}
