/*
 * Copyright (c) 2022 The National Archives
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package uk.gov.nationalarchives.omega.editorial.controllers

import javax.inject._
import play.api._
import play.api.i18n.I18nSupport.RequestWithMessagesApi
import play.api.i18n.Messages
import play.api.mvc._
import uk.gov.nationalarchives.omega.editorial._
import uk.gov.nationalarchives.omega.editorial.controllers.helpers.UserAction
import uk.gov.nationalarchives.omega.editorial.models.Credentials
import uk.gov.nationalarchives.omega.editorial.models.dao.{ SessionDAO, UserDAO }

import java.time.{ LocalDateTime, ZoneOffset }

/** This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject() (
  val userAction: UserAction,
  val messagesControllerComponents: MessagesControllerComponents
) extends MessagesAbstractController(messagesControllerComponents) {

  /** Create an Action to render an HTML page.
    *
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
  def index() = Action { implicit request: Request[AnyContent] =>
    withUser(user => Redirect(routes.EditSetController.view("1")))

  }

  private def withUser[T](block: Credentials => Result)(implicit request: Request[AnyContent]): Result = {
    val user = extractUser(request)

    user
      .map(block)
      .getOrElse(Redirect(routes.LoginController.view()))
  }

  private def extractUser(req: RequestHeader): Option[Credentials] = {
    val sessionTokenOpt = req.session.get("sessionToken")

    sessionTokenOpt
      .flatMap(token => SessionDAO.getSession(token))
      .filter(_.expiration.isAfter(LocalDateTime.now(ZoneOffset.UTC)))
      .map(_.username)
      .flatMap(UserDAO.getUser)
  }
}
