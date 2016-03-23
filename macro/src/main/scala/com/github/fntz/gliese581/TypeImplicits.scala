package com.github.fntz.gliese581

import com.rethinkdb.net.{Connection, Cursor}
import com.rethinkdb.gen.ast.ReqlFunction1
import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

object TypeImplicits {

  implicit class BooleanExt(left: Boolean) {
    def or(other: Boolean): ReqlFunction1 = macro ExtImpl.orImpl
    //    def and(other: Boolean): ReqlFunction1 = macro BooleanImpl.andImpl
  }

  implicit class ReqlFunction1Ext(r: ReqlFunction1) {
    def or(other: Boolean): ReqlFunction1 = macro ExtImpl.reqlOr
  }


}

class ExtImpl(val c: Context) extends MacroShare  {
  import c.universe._

  object traverser extends Traverser {
    var defs = List[(TermName, Tree)]()
    override def traverse(tree: Tree): Unit = tree match {
      case DefDef(_, methodName, _, List(List(ValDef(_, pname, _, _))), _, rhs) if methodName == TermName("apply") =>
        defs = (pname, rhs) :: defs
      case _ => super.traverse(tree)
    }
  }

  def parseBool(arg: TermName, tree: Tree) = {
    tree match {
      case q"$expr.$tname(..$exprs)" =>
        val field = expr match {
          case q"$obj.$field" =>
            q"$field"

          case _ =>
            c.abort(expr.pos, s"Cannot to parse $expr")
            q""
        }
        q"$arg.g(${field.toString}).${toR(tname)}(..$exprs)"

      case _ =>
        c.abort(c.enclosingPosition, s"Cannot to parse $tree")
        q""
    }
  }


  def reqlOr(other: c.Expr[Boolean]): c.Expr[ReqlFunction1] = {
    val fresh = TermName(c.freshName())
    traverser.traverse(c.prefix.tree)
    if (traverser.defs.isEmpty) {
      c.abort(c.enclosingPosition, s"Cannot to parse ${c.prefix.tree}")
    }

    val (arg, left) = traverser.defs.head
    val right = parseBool(arg, other.tree)

    val tree = c.untypecheck(q"$left.or($right)")

    println(fresh)
    println(tree.toString().replaceAll(arg.toString, fresh.toString))

    val result = q"""
       new ReqlFunction1 {
         override def apply($arg: $pkg.gen.ast.ReqlExpr): AnyRef = $arg.g("age").eq(10)
       }
      """
    println(result)




    c.Expr[ReqlFunction1](result)
  }

  def orImpl(other: c.Expr[Boolean]): c.Expr[ReqlFunction1] = {
    val arg = TermName(c.freshName())

    val right = other.tree match {
      case q"$expr.$tname(..$exprs)" =>
        val field = expr match {
          case q"$obj.$field" =>
            q"${field}"

          case _ =>
            c.abort(expr.pos, s"Cannot to parse $expr")
            q""
        }
        q"$arg.g(${field.toString}).${toR(tname)}(..$exprs)"

      case _ =>
        c.abort(c.enclosingPosition, s"Cannot to parse $other")
        q""
    }


    val left = c.prefix.tree.children.last match {
      case q"$expr.$tname(..$exprs)" =>
        val field = expr match {
          case q"$obj.$field" =>
            q"${field}"

          case _ =>
            c.abort(expr.pos, s"Cannot to parse $expr")
            q""
        }
        q"$arg.g(${field.toString}).${toR(tname)}(..$exprs)"

      case _ =>
        c.abort(c.enclosingPosition, s"Cannot to parse $other")
        q""
    }

    val result = q"""
       new ReqlFunction1 {
         override def apply($arg: $pkg.gen.ast.ReqlExpr): AnyRef = $left.or($right)
       }
      """
    c.Expr[ReqlFunction1](result)
  }
}
