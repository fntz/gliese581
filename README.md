
# Gliese581 - rethinkdb for scala (rethinkdb-java underhood)

## Features:

### typesafe
### dsl for selecting and aggregation
### based on java driver

```scala
import com.github.fntz.gliese581.rethinkify

@rethinkify case class Star(id: Option[String], name: String, age: Int)

val gliese = Star(None, "Gliese581", 1000000000)
gliese.toHM // => {name=Gliese581, age=1000000000}

```

### Convert from rethink to scala object via shapeless:

```scala
import com.github.fntz.gliese581.RethinkTransformer
val p = Person(None, "name", 20)
val toHashMap = p.toMap // => hash map
val hashMap = // some hash map
val person = RethinkTransformer.to[Person].from(hashMap)
```

### Selecting data

```scala

import com.github.fntz.gliese581.Implicits._
import com.github.fntz.gliese581.TypeImplicits._

case class Star(id: Option[String], name: String, age: Int) extends Rethinkify

implicit val r = RethinkDB.r
implicit val c = r.connection().hostname("localhost").port(28015).connect()

// return all in collection

val xsAll = r.t[Person].all.run(c) // => Stream[Option[Person]]

// one document

val one = r.t[Person].get("some-uuid") // => Option[Person]

// by filter

val f1 = r.t[Person].filter(p => p.age > 10).run(c) // => Stream[Option[Person]]
val f2 = r.t[Person].fitler(p => p.name == "some-name" and p.age > 10).run(c) // => Stream[Option[Person]]

```










