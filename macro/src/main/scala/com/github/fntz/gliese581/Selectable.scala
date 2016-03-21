package com.github.fntz.gliese581

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

class SelectableImpl(val c: Context) {

  import c.universe._

  object traverser extends Traverser {
    var applies = List[Apply]()

    override def traverse(tree: Tree): Unit = tree match {
      case app @ Apply(fun, args) =>
        applies = app :: applies
        super.traverse(fun)
        super.traverseTrees(args)
      case _ => super.traverse(tree)
    }
  }

  def scala2rethinkMap = Map(
    "$eq$eq" -> "eq",
    "$bang$eq" -> "ne",
    "$greater" -> "gt",
    "$less" -> "lt",
    "$greater$eq" -> "ge",
    "$less$eq" -> "le",
    "$bar$bar" -> "or",
    "$amp$amp" -> "and"
  )

  def toR(x: TermName) = {
    TermName(scala2rethinkMap(s"${x.encodedName}"))
  }
  def toR(x: Name) = {
    TermName(scala2rethinkMap(s"${x.encodedName}"))
  }
  // TODO nested fields
  // TODO pass optArgs
  // TODO if support
  // I need transform scala ast to ReqlFunction1
  def filter[C: c.WeakTypeTag, X: c.WeakTypeTag]
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

        traverser.traverse(t)
        val last = if (traverser.applies.size == 1) { 1 } else { traverser.applies.size - 1}


        def toF(x: Tree) = x match {
          case q"$o.$m(..$t)" =>
            m.toString()
          case q"$o.$m" =>
            m.toString
          case x =>
            c.abort(x.pos, s"Cannot parse $x in $expr")
            ""
        }

        def parse1(t: Tree): Option[Tree] = t match {
          case Apply(Select(Select(obj, field), op), args) =>
            Some(q"$arg.g(${field.toString}).${toR(op)}(..$args)")

          case Apply(Select(ltrl, op), List(Select(idnt, field))) =>
            Some(q"$arg.g(${field.toString}).${toR(op)}($ltrl)")

          case _ => None
//          case Apply(_, List(Apply(Select(_, op3), _))) =>
//            q"${toR(op3)}"

        }

//        println(parse1(t))


        def parse(t: Tree): Tree = t match {
          case Apply(Select(Select(obj, field), op), args) =>
            q"$arg.g(${field.toString}).${toR(op)}(..$args)"

          case Apply(Select(ltrl, op), List(Select(idnt, field))) =>
            q"$arg.g(${field.toString}).${toR(op)}($ltrl)"

          case Apply(Select(Apply(Select(Select(obj, field), op1), args), op2), List(Apply(Select(Select(obj1, field2), op3), List(args1)))) =>
            q"$arg.g(${field.toString}).${toR(op1)}(..$args).${toR(op2)}($arg.g(${field2.toString}).${toR(op3)}(..$args1))"

          
          case x =>
            println("----")
            println(showRaw(x))
            x.children.map(parse).reduce((a, b) => q"$a($b)")
        }

        println(t.children.map(x => parse(x)))

        val rrr = parse(t)
        println(rrr)


        q"""
          new $pkg.gen.ast.ReqlFunction1 {
            override def apply($arg: $pkg.gen.ast.ReqlExpr): AnyRef = $rrr
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