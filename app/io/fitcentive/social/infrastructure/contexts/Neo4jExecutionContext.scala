package io.fitcentive.social.infrastructure.contexts

import play.api.libs.concurrent.CustomExecutionContext

import javax.inject.{Inject, Singleton}

@Singleton
class Neo4jExecutionContext @Inject()(actorSystem: akka.actor.ActorSystem)
  extends CustomExecutionContext(actorSystem, "contexts.neo4j-execution-context")
