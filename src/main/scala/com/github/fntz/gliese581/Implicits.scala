package com.github.fntz.gliese581

import com.rethinkdb.RethinkDB
import com.rethinkdb.net.Connection

object Implicits {

  implicit class rext(r: RethinkDB) {
    import scala.collection.JavaConversions._
    def t[T](tn: String) = TypeSafeTable[T](r.table(tn))
    def createTableIfNotExists(tableName: String)(implicit c: Connection): Option[String] = {
      val res: java.util.ArrayList[String] = r.tableList().run(c)
      if (res.contains(tableName)) {
        res.find(x => x == tableName)
      } else {
        r.tableCreate(tableName).run(c)
        Some(tableName)
      }
    }
  }

}
