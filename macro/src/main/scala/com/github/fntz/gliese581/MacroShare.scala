package com.github.fntz.gliese581

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

trait MacroShare {
  val c: Context
  import c.universe._

  val pkg = q"com.rethinkdb"
  val ast = q"$pkg.gen.ast"

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

}
