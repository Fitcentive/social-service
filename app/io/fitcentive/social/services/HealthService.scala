package io.fitcentive.social.services

import com.google.inject.ImplementedBy
import io.fitcentive.social.infrastructure.settings.AppHealthService

import scala.concurrent.Future

@ImplementedBy(classOf[AppHealthService])
trait HealthService {
  def isGraphDatabaseAvailable: Future[Boolean]
}
