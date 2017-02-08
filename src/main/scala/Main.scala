
import java.util

import com.github.fntz.gliese581.format._
import com.rethinkdb.RethinkDB
import com.rethinkdb.net.{Connection, Cursor}

case class Person(name: String, age: Int)


object Main extends App {

  import scala.collection.JavaConversions._

  import RethinkImplicits._
  implicit val r = RethinkDB.r
  implicit val c = r.connection().hostname("localhost").port(28015).connect()

  try {

    implicit val personWrite = new Write[Person] {
      override def write(x: Person): util.HashMap[String, Any] = {
        val m = new util.HashMap[String, Any](2)
        m.put("name", x.name)
        m.put("age", x.age)
        m
      }
    }

    val p = Person("mike", 20)

    val persons = r.t[Person]("persons")

//    val t = persons.insert(p)

    val z: Cursor[_] = r.table("persons").run(c)

    println("@"*100)
    println(z.toList)

  } catch {
    case x: Throwable =>
      println(x)
  } finally {
    c.close()
  }


  /*

    case class Person(id: UUID, name: String, age: Int)

    implicit val personFormat = Macro.format[Person]

    implicit val r = RethinkDB.r
    implicit val c = r.connection().hostname("localhost").port(28015).connect()

    val persons = r.table[Person]("persons")

    persons.insert(any_person)

    persons.filter(p => p.age > 18)


   */

}
