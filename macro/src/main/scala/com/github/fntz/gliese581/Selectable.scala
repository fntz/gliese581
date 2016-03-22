package com.github.fntz.gliese581

import java.util.{HashMap => HM}

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

  // TODO how to avoid this
  def filter[X](f: T => ReqlFunction1)(implicit c: Connection): Cursor[X] = {
    t.filter(f.apply(null.asInstanceOf[T])).run(c)
  }

//  def filter[X](f: T => Boolean): Cursor[X] =
//    macro SelectableImpl.filter[T, X]
}

class SelectableImpl(val c: Context) extends MacroShare {
  import c.universe._

  // TODO nested fields
  // TODO pass optArgs
  // TODO if support
  // I need transform scala ast to ReqlFunction1
  def filter[C: c.WeakTypeTag, X: c.WeakTypeTag]
             (f: c.Expr[C => Boolean]): c.Expr[Cursor[X]] = {
    import c.universe._

    val tpe = c.weakTypeOf[C]
    val self = c.prefix.tree

    //q"(..$params) => $expr"
    val result = f.tree match {
      case q"(..$params) => $expr" => // params = tpe
        // then i should check if expression is simple ~> not block
        expr match {
          case x: Block =>
            c.abort(expr.pos, """Block instead of simple expression like: `p => p.name == "foo"`""")
          case _ =>
        }

        // just transform scala booleans from rethink ast
        // match => contains
        // TODO hasFields -> isPresent
        val arg = TermName(c.freshName("arg"))

        val result = expr match {
          case Apply(Select(Select(obj, field), op), args) =>
            q"$arg.g(${field.toString}).${toR(op)}(..$args)"

          case Apply(Select(ltrl, op), List(Select(idnt, field))) =>
            q"$arg.g(${field.toString}).${toR(op)}($ltrl)"

          case x =>
            c.abort(x.pos, s"Cannot to parse expression $expr")
            q""
        }


        q"""
          new $pkg.gen.ast.ReqlFunction1 {
            override def apply($arg: $pkg.gen.ast.ReqlExpr): AnyRef = $result
          }
        """

      case _ =>
        c.abort(c.enclosingPosition, s"Cannot parse $f in scala term")
        q""
    }

    val x = q"""
       $self.underlying.filter($result).run(implicitly[$pkg.net.Connection])
     """

    c.Expr[Cursor[X]](x)
  }
}