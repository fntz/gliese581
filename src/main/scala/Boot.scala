

import java.util

import com.rethinkdb.RethinkDB
import com.rethinkdb.gen.ast.{ReqlExpr, ReqlFunction1, Table}
import com.rethinkdb.gen.exc.{ReqlError, ReqlQueryLogicError}
import com.rethinkdb.model.MapObject
import com.rethinkdb.net.{Connection, Cursor}
import com.github.fntz.gliese581._
import java.util.{UUID => uuid}


object Boot extends App {

  import Implicits._
  import TypeImplicits._
  def g = uuid.randomUUID().toString

  implicit val map2Rethink = new Rethinkify[Map[String, Int]] {
    override def toHM(value: Map[String, Int]): util.HashMap[String, Any] = {
      val hm = new util.HashMap[String, Any]()
      value.foreach { case _ @ (k, v) =>
        hm.put(k, v)
      }
      hm
    }
  }

  @rethinkify
  case class Person(id: Option[String],
                    name: String, age: Int)


  implicit val r = RethinkDB.r
  implicit val c = r.connection().hostname("localhost").port(28015).connect()

  val xs = r.t[Person]("persons").filter { p =>
    p.age == 10 || p.age == 70 || p.age == 30   //(p.age == 10 && p.name == "zikurat")
  }.toList



}
