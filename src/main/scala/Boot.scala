

import java.util
import scala.language.existentials
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


  case class Person(id: Option[String],
                    name: String, age: Long) extends Rethinkify

  implicit val r = RethinkDB.r
  implicit val c = r.connection().hostname("localhost").port(28015).connect()

  val name = uuid.randomUUID().toString
  val p = Person(None, name, 100)
  import Rethinkify._
  val x = p.toMap

  import shapeless._
  val transformer = RethinkTransformer.to[Person].from(p.toMap)
  val table = "persons"

  import scala.collection.JavaConversions._


  val tt = r.t[Person] _
  type U = util.HashMap[String, Any]
  import java.util.{ArrayList => AL}
  import com.rethinkdb.model.{GroupedResult => GR}
//  val gs: AL[GR[Long, U]] = r.table(table).group("name").run(c)
//  gs.foreach { case x => println(x.group) }

//  val g1 = new TReqlGroup(r.table(table).group("name")) { type Out = Person } .run(c)
//  println(g1)

  val zs = tt(table).groupBy(_.age).run(c)
  println(zs.map(_._1.getClass))

//  val xs = tt(table)
//    .filter(p => p.age > 1)
//    .run(c)
//
//  println(xs.toList)

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

//r.db("test").tableCreate("persons").run(c)
//(0 to 10).foreach { i =>
//  val p = Person(None, s"name-$i", age = 10 * i)
//  r.t[Person]("persons").insert(p).run(c)
//}
