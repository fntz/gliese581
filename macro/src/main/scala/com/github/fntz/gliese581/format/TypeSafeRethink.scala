package com.github.fntz.gliese581.format


import com.rethinkdb.gen.ast.Table
import com.rethinkdb.net.Connection
import com.github.fntz.gliese581.util.HashMapImplicits
import java.util.{UUID, HashMap => HM}

class TypeSafeRethink[T](underlying: Table) {

  import scala.collection.JavaConversions._
  import HashMapImplicits._
  import InsertResult.{Fields => F}


  def filter(x: T => Boolean)(implicit c: Connection) = {

  }

  def isEmpty()(implicit c: Connection): Boolean = underlying.isEmpty.run(c)

  def insert[W : Write](x: W)(implicit c: Connection): InsertResult = {
    val w = implicitly[Write[W]]
    val result: HM[String, Any] = underlying.insert(w.write(x)).run(c)

    def getLong(key: String) = {
      result.optGet[Long](key).getOrElse(0L)
    }

    val deleted: Long = getLong(F._deleted)
    val inserted: Long = getLong(F._inserted)
    val unchanged: Long = getLong(F._unchanged)
    val replaced: Long = getLong(F._replaced)
    val errors: Long = getLong(F._errors)
    val skipped: Long = getLong(F._skipped)
    val generatedKeys: Iterable[UUID] =
      result.getOrDefault(F._generatedKeys, Nil)
        .asInstanceOf[java.util.ArrayList[String]]
      .map(uuid => UUID.fromString(uuid))

    InsertResult(
      deleted = deleted,
      inserted = inserted,
      unchanged = unchanged,
      generatedKeys = generatedKeys,
      replaced = replaced,
      errors = errors,
      skipped = skipped
    )
  }

}



case class InsertResult (
  deleted: Long,
  inserted: Long,
  unchanged: Long,
  replaced: Long,
  generatedKeys: Iterable[UUID],
  errors: Long,
  skipped: Long
)

object InsertResult {
  object Fields {
    val _deleted = "deleted"
    val _inserted = "inserted"
    val _unchanged = "unchanged"
    val _replaced = "replaced"
    val _errors = "errors"
    val _skipped = "skipped"
    val _generatedKeys = "generated_keys"
  }
}
