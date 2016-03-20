package com.github.gliese581

import java.util.{HashMap => HM}

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

  def filter[X](f: T => Boolean): Cursor[X] =
    macro SelectableImpl.filter[T, X]
}

object SelectableImpl {

  def scala2rethinkMap = Map(
    "$eq$eq" -> "eq",
    "$bang$eq" -> "ne",
    "$greater" -> "gt",
    "$less" -> "lt",
    "$greater$eq" -> "ge",
    "$less$eq" -> "le",
    "$bar$bar" -> "||",
    "$amp$amp" -> "&&"
  )


  // TODO nested fields
  // TODO pass optArgs
  // TODO if support
  // I need transform scala ast to ReqlFunction1
  def filter[C: c.WeakTypeTag, X: c.WeakTypeTag](c: Context)
             (f: c.Expr[C => Boolean]): c.Expr[Cursor[X]] = {
    import c.universe._
    val pkg = q"com.rethinkdb"
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
        val t: Tree = expr
        val arg = TermName(c.freshName("arg"))

        val r = t.children.collect {
          case q"$expr.$tname" =>
            //arg1.g("name").eq("qwe")
            val field = expr match {
              case q"$k.$m" =>
                q"""$arg.g(${m.toString})"""
              case _ =>
                c.abort(expr.pos, s"Cannot parse $expr")
                q""
            }
            val rOp = TermName(scala2rethinkMap(s"${tname.encodedName}"))
            q"$field.$rOp"

          case q"$lit" =>
            q"$lit"

          case x =>
            c.abort(c.enclosingPosition, s"Cannot parse expression $expr")
            q""
        }.reduce { (a, b) =>
          // check if literal and ()
          q"$a($b)"
        }

        q"""
          new $pkg.gen.ast.ReqlFunction1 {
            override def apply($arg: $pkg.gen.ast.ReqlExpr): AnyRef = $r
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