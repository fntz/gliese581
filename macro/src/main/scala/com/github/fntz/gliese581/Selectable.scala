package com.github.fntz.gliese581

import java.util.{HashMap => HM}

import com.rethinkdb.RethinkDB
import com.rethinkdb.gen.ast.{ReqlExpr, ReqlFunction1}
import com.rethinkdb.net.{Connection, Cursor}

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

trait Selectable[T] { self: TypeSafeTable[T] =>
  // TODO return models instead of HashMap
  type U = HM[String, AnyRef]
  private val t = self.underlying
  def all(implicit c: Connection): Cursor[_] = t.run(c)

  def get(key: String)(implicit c: Connection): U = t.get(key).run(c)
  def getOne(key: String)(implicit c: Connection) = get(key)

  def filter[X](f: T => ReqlFunction1)(implicit c: Connection): Cursor[X] = {
    t.filter(f.apply(null.asInstanceOf[T])).run(c)
  }

  def between = ???
}
