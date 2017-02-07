package com.github.fntz.gliese581.format

import scala.meta._
import scala.annotation.StaticAnnotation


class Write extends StaticAnnotation {

  inline def apply(defn: Any): Any = meta {

    println(defn)

    q"$defn"
  }

}
