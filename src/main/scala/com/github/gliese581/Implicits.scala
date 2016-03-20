package com.github.gliese581

import com.rethinkdb.RethinkDB

object Implicits {

  implicit class rext(r: RethinkDB) {
    def t[T](tn: String) = TypeSafeTable[T](r.table(tn))
  }

}
