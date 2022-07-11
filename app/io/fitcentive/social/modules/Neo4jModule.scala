package io.fitcentive.social.modules

import com.google.inject.{AbstractModule, Provides}
import io.fitcentive.social.domain.types.CustomTypes.GraphDb
import io.fitcentive.social.infrastructure.contexts.Neo4jExecutionContext
import io.fitcentive.social.services.SettingsService
import neotypes.GraphDatabase
import org.neo4j.driver.AuthTokens
import play.api.inject.ApplicationLifecycle

import javax.inject.Singleton
import scala.concurrent.Future

class Neo4jModule extends AbstractModule {

  @Provides
  @Singleton
  def provideNeo4jDriver(applicationLifecycle: ApplicationLifecycle, settingsService: SettingsService)(implicit
    neo4jExecutionContext: Neo4jExecutionContext
  ): GraphDb = {
    val driver = GraphDatabase.driver[Future](
      settingsService.neo4jConfig.databaseUri,
      AuthTokens.basic(settingsService.neo4jConfig.username, settingsService.neo4jConfig.password)
    )
    applicationLifecycle.addStopHook(() => driver.close)
    driver
  }

}
