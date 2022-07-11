package io.fitcentive.social.controllers

import play.api.mvc._

import javax.inject._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HealthController @Inject() (cc: ControllerComponents) extends AbstractController(cc) {

  def healthCheck: Action[AnyContent] = Action { Ok("Server is alive!") }

}
