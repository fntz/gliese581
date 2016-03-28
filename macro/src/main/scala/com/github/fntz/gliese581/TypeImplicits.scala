package com.github.fntz.gliese581

import com.rethinkdb.gen.ast.ReqlFunction1
import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

object TypeImplicits {

  implicit def bool2reql1(b: Boolean): ReqlFunction1 = macro ExtImpl.bool2reql1

  implicit class BooleanExt(left: Boolean) {
    def or(other: Boolean): ReqlFunction1 = macro ExtImpl.orImpl
    def and(other: Boolean): ReqlFunction1 = macro ExtImpl.andImpl
  }

  implicit class ReqlFunction1Ext(r: ReqlFunction1) {
    def or(other: Boolean): ReqlFunction1 = macro ExtImpl.reqlOr
    def and(other: Boolean): ReqlFunction1 = macro ExtImpl.reqlAnd
  }

}

class ExtImpl(val c: Context) extends MacroShare  {
  import c.universe._

  private def reql(arg: TermName, body: Tree): Tree = {
    q"""
      new $ast.ReqlFunction1 {
        override def apply($arg: $ast.ReqlExpr): AnyRef =
           $body
        }
    """
  }

  private object traverser extends Traverser {
    var defs = List[(TermName, Tree)]()
    override def traverse(tree: Tree): Unit = tree match {
      case DefDef(_, methodName, _, List(List(ValDef(_, pname, _, _))), _, rhs)
        if methodName == TermName("apply") =>
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

  def bool2reql1(b: c.Expr[Boolean]): c.Expr[ReqlFunction1] = {
    val fresh = TermName(c.freshName())
    val tree = parseBool(fresh, b.tree)
    c.Expr[ReqlFunction1](reql(fresh, tree))
  }

  def reqlOr(other: c.Expr[Boolean]): c.Expr[ReqlFunction1] = {
    buildReql(TermName("or"), other)
  }

  def reqlAnd(other: c.Expr[Boolean]): c.Expr[ReqlFunction1] = {
    buildReql(TermName("and"), other)
  }

  def buildReql(method: TermName, other: c.Expr[Boolean]): c.Expr[ReqlFunction1] = {
    val fresh = TermName(c.freshName())
    traverser.traverse(c.prefix.tree)
    if (traverser.defs.isEmpty) {
      c.abort(c.enclosingPosition, s"Cannot to parse ${c.prefix.tree}")
    }

    val (arg, left) = traverser.defs.head
    val right = parseBool(fresh, other.tree)

    val transformer = new Transformer {
      override def transform(tree: Tree) = {
        val newTree = tree match {
          case Ident(i) if i == arg =>
            Ident(fresh)
          case x => x
        }
        super.transform(newTree)
      }
    }

    val tree = c.untypecheck(q"${transformer.transform(left)}.$method($right)")

    c.Expr[ReqlFunction1](reql(fresh, tree))
  }


  private def conctruct(arg: TermName, expr: Tree): Tree = {
    expr match {
      case q"$expr.$tname(..$exprs)" =>
        val field = getField(expr)
        q"$arg.g(${field.toString}).${toR(tname)}(..$exprs)"

      case _ =>
        c.abort(c.enclosingPosition, s"Cannot to parse $expr")
        q""
    }
  }

  def andImpl(other: c.Expr[Boolean]): c.Expr[ReqlFunction1] = {
    build(TermName("and"), other)
  }

  def orImpl(other: c.Expr[Boolean]): c.Expr[ReqlFunction1] = {
    build(TermName("or"), other)
  }

  private def build(method: TermName, other: c.Expr[Boolean]): c.Expr[ReqlFunction1] = {
    val fresh = TermName(c.freshName())

    val right = conctruct(fresh, other.tree)

    val left = conctruct(fresh, c.prefix.tree.children.last)

    c.Expr[ReqlFunction1](reql(fresh, q"$left.$method($right)"))
  }

}
