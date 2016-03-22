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

  def reqlOr(other: c.Expr[Boolean]): c.Expr[ReqlFunction1] = {
    val arg = TermName(c.freshName())

    /*
    DefDef(Modifiers(OVERRIDE), TermName("apply"), List(), List(List(ValDef(Modifiers(PARAM), TermName("fresh$macro$2"), TypeTree().setOriginal(Select(Select(Select(Select(Ident(com), com.rethinkdb), com.rethinkdb.gen), com.rethinkdb.gen.ast), com.rethinkdb.gen.ast.ReqlExpr)), EmptyTree))), TypeTree().setOriginal(Select(Ident(scala), TypeName("AnyRef"))), Apply(Select(Apply(Select(Apply(Select(Ident(TermName("fresh$macro$2")), TermName("g")), List(Literal(Constant("age")))), TermName("eq")), List(Literal(Constant(10)))), TermName("or")), List(Apply(Select(Apply(Select(Ident(TermName("fresh$macro$2")), TermName("g")), List(Literal(Constant("age")))), TermName("eq")), List(Literal(Constant(30)))))))

     */

    def parse(x: Tree): Tree = {
      x match {
        case DefDef(_, _, _, _, _, _) =>
          println("$"*100)
          q""
        case _ =>
          val xs = x.children.map(parse(_))
          if (xs.isEmpty)
            x
          else
            xs.reduce((a, b) => q"")
      }
    }
    val x = parse(c.prefix.tree)
    println("#"*100)
    println(x)





    val result = q"""
       new ReqlFunction1 {
         override def apply($arg: $pkg.gen.ast.ReqlExpr): AnyRef = $arg.g("age").eq(10)
       }
      """

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

