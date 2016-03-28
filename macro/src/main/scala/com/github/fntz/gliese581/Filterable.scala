package com.github.fntz.gliese581

import java.util.{HashMap => HM, ArrayList => AL}

import com.rethinkdb.gen.ast.{ReqlExpr, ReqlFunction1, Group}
import com.rethinkdb.model.GroupedResult
import com.rethinkdb.net.{Connection, Cursor}
import shapeless.{HList, LabelledGeneric}

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context
import scala.language.existentials

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

class TReqlArrayList[T <: ReqlExpr](val x: T) {
  import scala.collection.JavaConversions._
  type Out
  final type U = HM[String, Any]
  def run[R <: HList](c: Connection)
                     (implicit lgen: LabelledGeneric.Aux[Out, R],
                      fromMap: FromMap[R]
                     ): Stream[Option[Out]] = {
    val r: java.util.ArrayList[U] = x.run(c)

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

class TReqlGroup[T <: ReqlExpr, Out, K](val x: T) {
  final type U = HM[String, Any]

  import org.json.simple.JSONArray
  final type GR = com.rethinkdb.model.GroupedResult[K, JSONArray]

  import scala.collection.JavaConversions._

  def run[R <: HList](c: Connection)
                     (implicit lgen: LabelledGeneric.Aux[Out, R],
                      fromMap: FromMap[R]
                     ): Map[K, Iterable[Out]] = {
    val r: java.util.ArrayList[GR] = x.run(c)

    r.map { x =>
      x.group -> Iterable.empty
    }.toMap
  }
}

trait Filterable[T <: Rethinkify] { self: TypeSafeTable[T] =>
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

  def isEmpty = t.isEmpty

  def sample(number: Long) = {
    new TReqlArrayList(t.sample(number)) {
      type Out = T
    }
  }

  // TODO infinity Stream plz // Map[K, Iterable[T]]
  def groupBy[K](f: T => K): TReqlGroup[Group, T, K] = macro FilterableImpl.group[T, K]

    //macro FilterableImpl.group[T, K]

  def between = ???
}

class FilterableImpl(val c: Context) extends MacroShare {
  import c.universe._

  def group[T: c.WeakTypeTag, K: c.WeakTypeTag]
    (f: c.Expr[T => K]): c.Expr[TReqlGroup[Group, T, K]] = {

    val field = f.tree match {
      case q"(..$params) => $expr" =>
        q"${getField(expr)}"

      case _ =>
        c.abort(f.tree.pos, s"Cannot to parse $f")
        q""
    }


    val result = q"""
      new com.github.fntz.gliese581.TReqlGroup[$ast.Group, ${c.symbolOf[T].name}, ${c.symbolOf[K].name}](${c.prefix.tree}
      .underlying.group(${field.toString()}))
    """

    c.Expr[TReqlGroup[Group, T, K]](q"$result")
  }

}