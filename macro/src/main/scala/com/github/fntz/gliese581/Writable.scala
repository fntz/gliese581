package com.github.fntz.gliese581

import shapeless._

trait Writable[T <: Rethinkify] { self: TypeSafeTable[T] =>
  import Rethinkify._
  private val t = self.underlying
  def insert[R <: HList](x: T)
      (implicit lgen: LabelledGeneric.Aux[T, R],
       tmr: ToMapRec[R]) = {
    t.insert(x.toMap)
  }
  def update(x: T) = ???
  def replace(x: T) = ???
  def delete(x: T) = ???
  def sync(x: T) = ???

}
