package io.fitcentive.social.infrastructure.settings

import com.typesafe.config.Config
import io.fitcentive.sdk.config.{GcpConfig, JwtConfig, SecretConfig, ServerConfig}
import io.fitcentive.social.domain.config.{AppPubSubConfig, Neo4jConfig, SubscriptionsConfig, TopicsConfig}
import io.fitcentive.social.services.SettingsService
import play.api.Configuration

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfigService @Inject() (config: Configuration) extends SettingsService {

  override def pubSubServiceAccountStringCredentials: String =
    config.get[String]("gcp.pubsub.service-account-string-credentials")

  override def userServiceConfig: ServerConfig =
    ServerConfig.fromConfig(config.get[Config]("services.user-service"))

  override def pubSubConfig: AppPubSubConfig =
    AppPubSubConfig(
      topicsConfig = TopicsConfig.fromConfig(config.get[Config]("gcp.pubsub.topics")),
      subscriptionsConfig = SubscriptionsConfig.fromConfig(config.get[Config]("gcp.pubsub.subscriptions"))
    )

  override def neo4jConfig: Neo4jConfig = Neo4jConfig.fromConfig(config.get[Config]("neo4j"))

  override def secretConfig: SecretConfig = SecretConfig.fromConfig(config.get[Config]("services"))

  override def keycloakServerUrl: String = config.get[String]("keycloak.server-url")

  override def jwtConfig: JwtConfig = JwtConfig.apply(config.get[Config]("jwt"))

  override def gcpConfig: GcpConfig =
    GcpConfig(project = config.get[String]("gcp.project"))

}
