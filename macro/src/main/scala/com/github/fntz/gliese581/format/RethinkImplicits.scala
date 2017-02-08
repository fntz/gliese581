package com.github.fntz.gliese581.format


import com.rethinkdb.RethinkDB
import com.rethinkdb.net.Connection

object RethinkImplicits {

  implicit class RExtensions(r: RethinkDB) {
    def t[T](tableName: String) = new TypeSafeRethink(r.table(tableName))
  }

}
