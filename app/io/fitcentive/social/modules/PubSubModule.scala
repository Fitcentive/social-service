package io.fitcentive.social.modules

import com.google.auth.Credentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.inject.{AbstractModule, Provides}
import io.fitcentive.sdk.gcp.pubsub.PubSubPublisher
import io.fitcentive.social.services.SettingsService

import javax.inject.Singleton

class PubSubModule extends AbstractModule {

  @Provides
  @Singleton
  def provideGcpCredentials: Credentials =
    ServiceAccountCredentials
      .fromStream(getClass.getResourceAsStream("/fitcentive-1210-11ad1b0805a3.json"))
      .createScoped()

  @Provides
  @Singleton
  def providePubSubPublisher(settingsService: SettingsService): PubSubPublisher =
    new PubSubPublisher(settingsService.gcpConfig.credentials, settingsService.gcpConfig.project)

}
