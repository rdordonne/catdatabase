package models

import play.api.libs.json._

case class Cat(id: Long, name: String, age: Int, color: String, picture: String, race: String, gender: Int)

object Cat {
  
  implicit val catFormat = Json.format[Cat]

}

