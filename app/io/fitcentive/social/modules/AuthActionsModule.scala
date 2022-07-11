package io.fitcentive.social.modules

import com.google.inject.{AbstractModule, Provides}
import io.fitcentive.sdk.config.JwtConfig
import io.fitcentive.sdk.domain.{PublicKeyRepository, TokenValidationService}
import io.fitcentive.sdk.infrastructure.{AuthTokenValidationService, CachedKeycloakPublicKeyRepository, KeycloakPublicKeyRepository}
import io.fitcentive.social.services.SettingsService

import javax.inject.Singleton

class AuthActionsModule extends AbstractModule {

  /**
    * NOTE: The following 3 providers include sdk dependencies for Auth actions
    * Without them, you will not be able to invoke authentication actions and will run into runtime Guice errors
    */

  @Provides
  @Singleton
  def provideJwtConfig(settingsService: SettingsService): JwtConfig =
    settingsService.jwtConfig

  @Provides
  @Singleton
  def provideCachedKeycloakPublicKeyRepository(settingsService: SettingsService): PublicKeyRepository = {
    val underlying = new KeycloakPublicKeyRepository(settingsService.keycloakServerUrl)
    new CachedKeycloakPublicKeyRepository(underlying)
  }

  @Provides
  @Singleton
  def provideJwtTokenValidationService(
    settingsService: SettingsService,
    publicKeyRepository: PublicKeyRepository
  ): TokenValidationService =
    new AuthTokenValidationService(settingsService.jwtConfig, settingsService.secretConfig, publicKeyRepository)

}
