package controllers

import javax.inject.{Inject, Singleton}

import model.User
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc.{Action, BodyParsers, Controller}
import slick.driver.JdbcProfile
import model.UserJson._

import scala.concurrent.Future

@Singleton
class DbController @Inject()(dbConfigProvider: DatabaseConfigProvider)
    extends Controller {

  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig.driver.api._

  class Table1(tag: Tag) extends Table[User](tag, "table1") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def first = column[String]("first")

    def last = column[String]("last")

    def * = (id.?, first, last) <> (User.tupled, User.unapply)
  }

  class Table2(tag: Tag) extends Table[User](tag, "table2") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def first = column[String]("first")

    def last = column[String]("last")

    def * = (id.?, first, last) <> (User.tupled, User.unapply)
  }

  val table1 = TableQuery[Table1]
  val table2 = TableQuery[Table2]

  def set1() = Action.async(BodyParsers.parse.json) { request =>
    val userJson = request.body.validate[User]

    userJson fold (
      errors => Future.successful(BadRequest("Bad Request")),
      user =>
        dbConfig.db
          .run(table1 returning table1.map(_.id) += user)
          .map(id => Created(id.toString))
    )

  }

  def set2() = Action.async(BodyParsers.parse.json) { request =>
    val userJson = request.body.validate[User]

    userJson fold (
      errors => Future.successful(BadRequest("Bad Request")),
      user =>
        dbConfig.db
          .run(table2 returning table2.map(_.id) += user)
          .map(id => Created(id.toString))
    )

  }

  def get1(id: Long) = Action.async {

    val f = dbConfig.db.run(table1.filter(_.id === id).result.headOption)

    f.map {
      case None    => NotFound("Not Found")
      case Some(h) => Ok(Json.toJson(h))
    }

  }

  def get2(id: Long) = Action.async {

    val f = dbConfig.db.run(table2.filter(_.id === id).result.headOption)

    f.map {
      case None    => NotFound("Not Found")
      case Some(h) => Ok(Json.toJson(h))
    }

  }
}
