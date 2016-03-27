

import java.util

import com.rethinkdb.RethinkDB
import com.rethinkdb.gen.ast.{ReqlExpr, ReqlFunction1, Table, TableCreate}
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

  import scala.collection.JavaConversions._

  implicit val r = RethinkDB.r
  implicit val c = r.connection().hostname("localhost").port(28015).connect()

  import shapeless._
  import labelled.FieldType
  import record._

  //trait ToMapRec[L <: HList] { def apply(l: L): Map[String, Any] }

//LabelledGeneric.Aux[Boot.Person, lgen.Repr]
  val p = Person(None, "das", 100)

  val g1 = LabelledGeneric[Person]

  import Rethinkify._
  println(p.toMap)


  val transformer = RethinkTransformer.to[Person].from(p.toMap)
  println(transformer)
  val table = "persons"

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
