package io.fitcentive.social.controllers

import io.fitcentive.social.services.HealthService
import play.api.mvc._

import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class HealthController @Inject() (healthService: HealthService, cc: ControllerComponents)(implicit
  exec: ExecutionContext
) extends AbstractController(cc) {

  def readinessProbe: Action[AnyContent] =
    Action.async {
      healthService.isGraphDatabaseAvailable.map {
        case true => Ok("Server is alive!")
        case _    => NotFound
      }
    }

  def livenessProbe: Action[AnyContent] = Action { Ok("Server is alive!") }

}
