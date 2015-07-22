package controllers

import play.api._
import play.api.mvc._
import play.api.i18n._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.libs.json.Json
import play.api.libs.iteratee.Enumerator;
import play.api.mvc.Result;
import models._
import dal._

import scala.concurrent.{ ExecutionContext, Future }

import javax.inject._

class CatController @Inject() (repo: CatRepository, val messagesApi: MessagesApi)
                                 (implicit ec: ExecutionContext) extends Controller with I18nSupport{

  /**
   * The mapping for the cat form.
   */
  val catForm: Form[CatForm] = Form {
    mapping(
      "id" -> optional(longNumber),
      "name" -> nonEmptyText,
      "age" -> number.verifying(min(0), max(140)),
      "color" -> text,
      "picture"-> text,
      "race" -> text, 
      "gender" -> number.verifying(min(0), max(1))
    )(CatForm.apply)(CatForm.unapply)
  }

  
  /**
   * index action.
   */
  def index = Action { implicit request =>
    Ok(views.html.index(catForm))
  }
  
  
  /**
   * addOrUpdate cat action.
   */
  def addOrUpdateCat = Action.async { implicit request =>

    //Bind request to form object.
    catForm.bindFromRequest.fold(
     
      errorForm => {
        //Return to index with binding errors
        Future.successful(Ok(views.html.index(errorForm)))
      },
      // No binding errors
      cat => {
        //Extract and manage uploaded file
        val pictureBody = request.body.asMultipartFormData
        val picturePath = Utils.imageFileManager(pictureBody, Option(cat.picture))
        
        //Check presence of id
        cat.id match {
          case Some(id) => {
            // Update item information and return to index
            repo.update(id ,cat.name, cat.age, cat.color, picturePath, cat.race, cat.gender).map { _ =>
              Redirect(routes.CatController.index)
            }
          }
          case None => {
            //Create new item and return to index
            repo.create(cat.name, cat.age, cat.color, picturePath, cat.race, cat.gender).map { _ =>
              Redirect(routes.CatController.index)
            }
          }
        }
      }
    )
  }

  
  /**
   * A REST endpoint that gets all the cats as JSON.
   */
  def getCats = Action.async {
  	repo.list().map { cats =>
      Ok(Json.toJson(cats))
    }
  }
  
  
  /**
   * A REST endpoint that gets one cat matching id and returned as JSON.
   */
  def getCat(id: Long) = Action.async { implicit request =>
    repo.get(id).map { catOpt =>
      catOpt match {
      case Some(cat) => Ok(Json.toJson(cat))
      case None => NotFound(Messages("cat.error.notfound"))
      }
    }
  }

  
  /**
   * A REST endpoint that deletes one cat matching id.
   */
  def deleteCat(id: Long) = Action.async { implicit request =>
    repo.get(id).map { catOpt =>
      catOpt match {
        case Some(cat) => {
          if(!Utils.isBlank(Option(cat.picture))) {
            Utils.deletePicture(cat.picture)
          }
          repo.delete(id).map { delCount =>
            if(delCount>0)
              Ok(Messages("cat.delete.done"))
            else
              InternalServerError(Messages("cat.delete.failed"))
          }
          Ok(Messages("cat.delete.progress"))
        }
        case None =>
          NotFound(Messages("cat.error.notfound"))
      }
    }
  }
  
  
  /**
   * getAllCatsDetails action uses catDetails template to return the HTML formatted cat list.
   */
  def getAllCatsDetails = Action.async { implicit request =>
    repo.list().map { cats =>
      Ok( views.html.catDetails(cats) )
    }
  }
}


/**
 * Class representing a cat form.
 */
case class CatForm(id: Option[Long], name: String, age: Int, color: String, picture: String, race: String, gender: Int)
