package com.github.fntz.gliese581

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context


@compileTimeOnly("enable macro paradise to expand macro annotations")
class rethinkify extends StaticAnnotation {
  def macroTransform(annottees: Any*) = macro rethinkifyImpl.rethinkify
}

object rethinkifyImpl {
  def rethinkify(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    val hm = TermName(c.freshName("hashMap"))

    val newKlass = annottees.map(_.tree).collect {
      case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { $self => ..$stats }" =>

        if (!mods.hasFlag(c.universe.Flag.CASE)) {
          c.abort(c.enclosingPosition, "Use @rethinkify only with `case class`")
        }

        val pm = paramss.flatten.map {
          case q"$mods val $pat: $tpt = $expr" =>
            pat.toString -> tpt
        }.toMap

        val result = pm.collect { case _ @ (k, v) =>
          val cc = q"${Ident(TermName(k))}"
          v match {
            case Ident(TypeName(name)) =>

              name match {
                case "Boolean" | "Byte" | "Char" |
                     "Short" | "Int" | "Long" |
                     "Float" | "Double" | "String" =>
                  q"$hm.put($k, $cc)"

                case x =>
                  // ok, i should call toHM for it method or TODO implicitly[Rethinkify].toHM
                  q"$hm.put($k, $cc.toHM)"
              }
              //println(z)


            // Option, List, Map ...
            case AppliedTypeTree(tpe, typeParams) =>
              val z = AppliedTypeTree(tpe, typeParams)

              if (typeParams.size == 1) {
                tpe match {
                  case Ident(TypeName(name)) =>
                    name match {
                      case "Option" =>
                        q"""
                         ${Ident(TermName(k))}.map(v => $hm.put($k, v))
                       """

                      case x =>
                        q"""$hm.put($k, "1000")"""
                      //q"""$hm.put($k, implicitly[com.github.fntz.gliese581.Rethinkify[$m[..$xs]]].toHM)"""
                    }

                }
              } else {
                // Map ?
                q"""$hm.put($k, implicitly[com.github.fntz.gliese581.Rethinkify[$tpe[..$typeParams]]].toHM($cc))"""
              }

            case x =>
              c.abort(v.pos, s"Cannot call `rethinkify` for type $x")
              q""
          }
        }


        val toHM = q"""
           def toHM: java.util.HashMap[String, Any] = {
             val $hm = new java.util.HashMap[String, Any]()
             ..$result
             $hm
           }
         """

        q"""$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends
            { ..$earlydefns } with ..$parents {
          $self =>
          ..$stats
          ..$toHM
        }"""


      case _ =>
        c.abort(c.enclosingPosition, "Use @rethinkify only with `case class`")
    }

    c.Expr[Any](q"..$newKlass")
  }
}

trait Rethinkify[T] {
  def toHM(value: T): java.util.HashMap[String, Any]
}