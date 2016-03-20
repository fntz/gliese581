package com.github.fntz.gliese581

import com.rethinkdb.gen.ast.Table


case class TypeSafeTable[T](underlying: Table) extends Selectable[T]
