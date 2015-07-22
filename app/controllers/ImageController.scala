package controllers

import play.api._
import play.api.mvc._
import play.api.i18n._

import scala.concurrent.{ ExecutionContext, Future }

import javax.inject._

class ImageController @Inject() (val messagesApi: MessagesApi)
                                 (implicit ec: ExecutionContext) extends Controller with I18nSupport{
  
  /**
   * getImage returns the requested image.
   * 
   */
  def getImage(filename: String) = Action { implicit request =>
    val image = Utils.getPicture(filename)
    image match {
      case Some(file) => Ok.sendFile(file)
      case None => NotFound("File not found")
    }
  }
}