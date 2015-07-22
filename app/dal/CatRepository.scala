package dal

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import models.Cat
import scala.concurrent.{ Future, ExecutionContext }

/**
 * DB Handler for cat items.
 */
@Singleton
class CatRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  
  //Configuration items
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import driver.api._

  /**
   * Cats table definition
   */
  private class CatsTable(tag: Tag) extends Table[Cat](tag, "cats") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def age = column[Int]("age")
    def color = column[String]("color")
    def picture = column[String]("picture")
    def race = column[String]("race")
    def gender = column[Int]("gender")
    /**
     * Table's default projection
     */
    def * = (id, name, age, color, picture, race, gender) <> ((Cat.apply _).tupled, Cat.unapply)
  }

  private val cats = TableQuery[CatsTable]

  /**
   * Create a cat item.
   */
  def create(name: String, age: Int, color: String, picture: String, race: String, gender: Int): Future[Cat] = db.run {

    (cats.map(p => (p.name, p.age, p.color, p.picture, p.race, p.gender))
      returning cats.map(_.id)
      into ((catInfo, id) => Cat(id, catInfo._1, catInfo._2, catInfo._3, catInfo._4, catInfo._5, catInfo._6))
    ) += (name, age, color, picture, race, gender)
  }
  
  /**
   * Update a cat item.
   */
  def update(id: Long, name: String, age: Int, color: String, picture: String, race: String, gender: Int): Future[Int] = db.run {
    val cat = Cat(id,name,age,color, picture, race,gender)
    cats.filter(_.id===id).update(cat)
  }
  
  /**
   * Delete a cat item.
   */
  def delete(id: Long): Future[Int] = db.run {
    cats.filter(_.id===id).delete
  }

  /**
   * List all cat items in the database.
   */
  def list(): Future[Seq[Cat]] = db.run {
    cats.result
  }
  
  /**
   * Find the cat item matching id.
   */
  def get(id: Long): Future[Option[Cat]] = db.run {
    cats.filter(_.id===id).result.headOption
  }
}
