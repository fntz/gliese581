

import java.util

import com.rethinkdb.RethinkDB
import com.rethinkdb.gen.ast.Table
import com.rethinkdb.gen.exc.{ReqlError, ReqlQueryLogicError}
import com.rethinkdb.model.MapObject
import com.rethinkdb.net.{Connection, Cursor}

import com.github.gliese581._

object Boot extends App {

  import Implicits._
  import TypeImplicits._

  implicit val map2Rethink = new Rethinkify[Map[String, Int]] {
    override def toHM(value: Map[String, Int]): util.HashMap[String, Any] = {
      val hm = new util.HashMap[String, Any]()
      value.foreach { case _ @ (k, v) =>
        hm.put(k, v)
      }
      hm
    }
  }

  @rethinkify case class Id(id: Int)
  @rethinkify
  case class Person(id: Option[String],
                    name: String, age: Int,
                    xs: Vector[Int],
                    hm: Map[String, Int],
                    zxd: Id
                   )

  val p = Person(Some("123"), "foo", 123, Vector(1,2,3), Map("a" -> 1), Id(1))
  println(p.toHM)

//  implicit val r = RethinkDB.r
//  implicit val c = r.connection().hostname("localhost").port(28015).connect()

//  val xs = r.t[Person]("tv").filter { x =>
//    x.name == "zikurat" //|| (x.age > 55 || x.name == "zikurat")
//    //x.name != "zikurat" && x.name <= "zikurat1"
//  }
//
//  println(xs.toList)


//  object Implicits {
//    import java.util.{HashMap => HM}
//    implicit class TableExt(t: Table) {
//      def f[T](implicit c: Connection): Cursor[T] = t.run(c)
//      def all[T](implicit c: Connection): Cursor[T] = {
//        t.f
//      }
//      def getOne(id: String)(implicit c: Connection): HM[_, _]  = t.get(id).run(c)
//    }
//
//  }
//
//  import Implicits._
//
//  // modern filters
//
//
//  implicit val r = RethinkDB.r
//  implicit val c = r.connection().hostname("localhost").port(28015).connect()
//  r.db("test").tableCreate("tv").run(c)
//  r.table("tv").insert(r.hashMap("name", "zikurat")).run(c)
//  import java.util.{UUID => uuid}
//  def g = uuid.randomUUID().toString
//  (1 to 10).foreach { i =>
//    r.table("tv").insert(r.hashMap("name", s"${g}")).run(c)
//    Unit
//  }
//
////  val x: Cursor[_] = table("tv").ru
////  println(x.toList)
////com.rethinkdb.gen.ast.ReqlExpr row
//  //val x = table("tv").f
//
//  import com.rethinkdb.gen.ast.{ReqlExpr => re, _}
//
//  val f = new ReqlFunction1 {
//    override def apply(arg1: re): AnyRef = arg1.g("name").eq("qwe")
//  }
//
//
//  case class Name(id: String, name: String) {
//    def toHM = {
//      val hm = new util.HashMap[String, AnyRef]()
//      hm.put("name", name)
//      hm
//    }
//  }
//
//  val n = Name("asd", "qwe")
//
////  r.table("tv").insert(n.toHM).run(c)
//
//
////  val z: Cursor[_] = r.table("tv").filter(f).run(c)
////  val z = table("tv").all
//  import scala.collection.JavaConversions._
//  val z: util.ArrayList[_] = r.table("tv").group("name").run(c)

}
