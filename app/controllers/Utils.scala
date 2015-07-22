
package controllers
import play.api.mvc._
import play.api.libs.Files.TemporaryFile;
import java.util.concurrent.atomic.AtomicLong;
import java.io.File

object Utils {
  //Base path for uploaded images.
  val PATH_UPLOADED_IMG: String = "/tmp/images/uploaded/"
  
  //ID generator
  private val currentId: AtomicLong = new AtomicLong(0L);
  
  /**
   * initialization creates base path if necessary on application startup.
   */
  def initialization(): Boolean = {
    val baseDir = new File(PATH_UPLOADED_IMG)
    if (!baseDir.exists()) {
      try
        baseDir.mkdirs()
       catch {
         case e: Exception => println("Creation of base path failed:"+e.toString())
         false
       }
    }
    else
      true
  }
  
	def getID: Long = {
		currentId.incrementAndGet()
	}
  
  /**
   * isBlank checks if a string is empty or null.
   */
  def isBlank(input: Option[String]): Boolean = {
    input.filterNot(s => s == null || s.trim.isEmpty).isEmpty
  }
  
  /**
   * getFileExtension returns the extension of the given filename.
   */
  def getFileExtension(filename: String): String = {
    val pat = """(.*)[.]([^.]*)""".r
    filename match { 
      case pat(fn,ext) => ext
      case _ => ""
    }
  }
  
  /**
   * validateFileContentType validates that the given contentType is an image type.
   */
  def validateFileContentType(contentType: Option[String]): Boolean = {
    val pat = """^image/.*""".r
    contentType match { 
      case Some(str) => { str match {
        case pat() => true
        case _ => false
      }
      }
      case _ => false
    }
  }
  
  /**
   * getPicture retrieve given picture from file system.
   */
  def getPicture(pictureName: String): Option[File] = {
    val file = new File(PATH_UPLOADED_IMG+pictureName)
    if(file.exists())
      Option(file)
    else
      None
  }
  
  /**
   * imageFileManager migrates uploaded images from temporary location to PATH_UPLOADED_IMG and
   * generates proper names for them.
   */
  def imageFileManager(pictureBody: Option[MultipartFormData[TemporaryFile]], filename: Option[String]): String = {  
    pictureBody match { 
      case Some(x) => {
        if( x.files.size > 0 ){
          //Uploaded file confirmed.
          val tmpFile = x.files.head

         //Check contentType
          if(Utils.validateFileContentType(tmpFile.contentType)) {
            //Generate name if provided one is blank.
            val newFilename = if(!Utils.isBlank(filename))
                                filename.get
                              else 
                                Utils.getID.toString()+"."+ Utils.getFileExtension(tmpFile.filename)
            
            //Generate full path name.
            val fileDestPath = PATH_UPLOADED_IMG+newFilename
            
            //Delete destination file if it exists then move temporary file to new location.
            val newFile = new File(fileDestPath)
            if(newFile.exists)
              try
                newFile.delete
              catch {
                 case e: Exception => println("Deletion of existing file failed:"+e.toString())
                 return ""
               }
            try
              tmpFile.ref.moveTo(newFile)
            catch {
                 case e: Exception => println("Creation of new file failed:"+e.toString())
                 return ""
               }
            newFilename
          }
          else {
            //Invalid temporary file, deleted.
            try
              tmpFile.ref.file.delete
            catch {
                 case e: Exception => println("Deletion of temporary file failed:"+e.toString())
               }
            ""
          }
        }
        else
          ""
      }
      //No file uploaded
      case None => ""
    }
  }
  
  /**
   * deletePicture deletes given picture.
   */
  def deletePicture(pictureName: String): Boolean = {
    val file = new File(PATH_UPLOADED_IMG+pictureName)
    if(file.exists())
      try
        file.delete()
      catch {
         case e: Exception => println("Deletion of existing file failed:"+e.toString())
         return false
       }
    else
      false
  }
  
}