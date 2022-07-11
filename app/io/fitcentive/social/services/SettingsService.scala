package io.fitcentive.social.services

import com.google.inject.ImplementedBy
import io.fitcentive.sdk.config.{GcpConfig, JwtConfig, SecretConfig, ServerConfig}
import io.fitcentive.social.domain.config.{AppPubSubConfig, Neo4jConfig}
import io.fitcentive.social.infrastructure.settings.AppConfigService

@ImplementedBy(classOf[AppConfigService])
trait SettingsService {
  def pubSubConfig: AppPubSubConfig
  def gcpConfig: GcpConfig
  def jwtConfig: JwtConfig
  def keycloakServerUrl: String
  def secretConfig: SecretConfig
  def neo4jConfig: Neo4jConfig
  def userServiceConfig: ServerConfig
}
