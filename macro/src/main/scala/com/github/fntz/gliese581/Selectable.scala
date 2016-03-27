package com.github.fntz.gliese581

import java.util.{HashMap => HM}

import com.rethinkdb.gen.ast.{ReqlExpr, ReqlFunction1}
import com.rethinkdb.net.{Connection, Cursor}
import shapeless.{HList, LabelledGeneric}

import scala.language.experimental.macros

class TReqlStream[T <: ReqlExpr](val x: T) {
  import scala.collection.JavaConversions.asScalaIterator
  type Out
  final type U = HM[String, Any]
  def run[R <: HList](c: Connection)
                     (implicit lgen: LabelledGeneric.Aux[Out, R],
                      fromMap: FromMap[R]
                     ): Stream[Option[Out]] = {
    val r: Cursor[U] = x.run(c)

    (for {
      q <- r
    } yield {
      RethinkTransformer.to[Out].from(q)
    }).toStream
  }
}

class TReqlOnce[T <: ReqlExpr](val x: T) {
  type Out
  final type U = HM[String, Any]
  def run[R <: HList](c: Connection)
                              (implicit lgen: LabelledGeneric.Aux[Out, R],
                               fromMap: FromMap[R]
                              ): Option[Out] = {
    RethinkTransformer.to[Out].from(x.run(c))
  }
}

trait Selectable[T <: Rethinkify] { self: TypeSafeTable[T] =>
  private val t = self.underlying
  def all = {
    new TReqlStream(t) {
      type Out = T
    }
  }

  def get(key: String) = {
    new TReqlOnce(t.get(key)) {
      type Out = T
    }
  }

  def getOne(key: String) = get(key)

  def filter(f: T => ReqlFunction1) = {
    new TReqlStream(t.filter(f.apply(null.asInstanceOf[T]))) {
      type Out = T
    }
  }

  def between = ???
}
