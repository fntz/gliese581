

import java.util

import com.rethinkdb.RethinkDB
import com.rethinkdb.gen.ast.{ReqlExpr, Table, TableCreate}
import com.rethinkdb.gen.exc.{ReqlError, ReqlQueryLogicError}
import com.rethinkdb.model.MapObject
import com.rethinkdb.net.{Connection, Cursor}
import com.github.fntz.gliese581._
import java.util.{UUID => uuid}


object Boot extends App {

  import Implicits._
  import TypeImplicits._
  def g = uuid.randomUUID().toString


  //@rethinkify
  case class Person(id: Option[String],
                    name: String, age: Long) extends Rethinkify

  implicit val r = RethinkDB.r
  implicit val c = r.connection().hostname("localhost").port(28015).connect()

  val name = uuid.randomUUID().toString
  val p = Person(None, name, 100)
  import Rethinkify._
  val x = p.toMap


  val transformer = RethinkTransformer.to[Person].from(p.toMap)
  val table = "persons"

  val tt = r.t[Person] _


  val xs = tt(table).filter(p => p.age > 1 and p.name == "some-name").run(c)
//  println(xs.toList.flatten)
//  println(tt(table).get("de6db8d0-35eb-4f8a-9fa4-8c4c064d47cf").run(c))

  println(tt(table).all.run(c).toList)

//  val xs = r.t[Person](table).filter { p =>
//    p.age > 30
//    //(p.age == 10 or p.age == 30) or p.name == "name-4" or p.name == "name-5" //(p.age == 10 && p.name == "zikurat")
//  }.toList

//  println(xs)
//
//  xs.asInstanceOf[java.util.ArrayList[util.HashMap[String, Any]]]
//    .foreach { x =>
//      //println(x)
//      println(transformer.from(x))
//    }






//  val xs1: Cursor[_] = r.table(table).filter(f).run(c)
//  println("---")
//  println(xs1.toList)
//  val xs11 = xs1.toList.to[Vector].asInstanceOf[Vector[util.HashMap[String, Any]]]
//  println(xs11)
//  println(xs11.map(p => RethinkTransformer.to[Person].from(p)))

}

//  r.db("test").tableCreate("persons").run(c)
//  (0 to 10).foreach { i =>
//    val p = Person(None, s"name-$i", age = 10 * i)
//    r.table("persons").insert(p.toHM).run(c)
//    Unit
//  }
