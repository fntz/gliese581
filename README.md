
# Gliese581 - rethinkdb for scala (rethinkdb-java underhood)

## Features:

### Automatic conversion via `@rethinkify` annotation (add `toHM` method in case class)


```scala
import com.github.fntz.gliese581.rethinkify

@rethinkify case class Star(id: Option[String], name: String, age: Int)

val gliese = Star(None, "Gliese581", 1000000000)
gliese.toHM // => {name=Gliese581, age=1000000000}

```

### TypeSafe operations:

```scala

import com.github.fntz.gliese581.Implicits._

@rethinkify case class Star(id: Option[String], name: String, age: Int)

implicit val r = RethinkDB.r
implicit val c = r.connection().hostname("localhost").port(28015).connect()

// works fine
val xs = r.t[Star]("tv").filter { star =>
  star.name == "Gliese581"
}
pritnln(xs.toList)


```







