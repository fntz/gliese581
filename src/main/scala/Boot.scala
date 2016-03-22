

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

  val table = "persons"

  val f = new ReqlFunction1 {
    override def apply(arg1: ReqlExpr): AnyRef = arg1.g("age").eq(10).or(arg1.g("age").eq(30))
  }

  val xs = r.t[Person](table).filter { p =>
    (p.age == 10 or p.age == 30) or p.name == "name-4" //(p.age == 10 && p.name == "zikurat")
  }


  println(xs.toList)


//  val xs1: Cursor[_] = r.table(table).filter(f).run(c)
//  println("---")
//  println(xs1.toList.size)

}

//  r.db("test").tableCreate("persons").run(c)
//  (0 to 10).foreach { i =>
//    val p = Person(None, s"name-$i", age = 10 * i)
//    r.table("persons").insert(p.toHM).run(c)
//    Unit
//  }
