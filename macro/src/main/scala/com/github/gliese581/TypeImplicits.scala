package com.github.gliese581
import java.util

trait Rethinkify[T] {
  def toHM(value: T): java.util.HashMap[String, Any]
}

object TypeImplicits {



//  object CollectionImplicits {
//    implicit val rethinkifyVector = new Rethinkify[Vector[_]] {
//      override def toHM(value: Vector[_]): util.HashMap[String, Any] = {
//        val hm = new util.HashMap[String, Any]()
//        hm.put("zik", value)
//        h
//      }
//    }
//  }

}
