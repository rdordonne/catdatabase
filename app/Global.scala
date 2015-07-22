import play.api._
import controllers.Utils

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    Logger.info("Application has started")
    if(!Utils.initialization())
      Logger.info("Base path not created")
  }
}