package io.fitcentive.social.infrastructure.utils

import io.fitcentive.social.services.SettingsService
import play.api.libs.ws.WSRequest

trait ServiceSecretSupport {

  implicit class ServiceSecretHeaders(wsRequest: WSRequest) {
    def addServiceSecret(settingsService: SettingsService): WSRequest =
      wsRequest.addHttpHeaders("Service-Secret" -> settingsService.secretConfig.serviceSecret)
  }
}
