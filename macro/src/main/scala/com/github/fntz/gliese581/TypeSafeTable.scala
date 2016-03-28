package com.github.fntz.gliese581

import com.rethinkdb.gen.ast.Table


case class TypeSafeTable[T <: Rethinkify](underlying: Table)
  extends Filterable[T] with Writable[T]
