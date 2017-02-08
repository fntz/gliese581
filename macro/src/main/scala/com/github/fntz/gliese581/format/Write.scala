package com.github.fntz.gliese581.format

//import scala.meta._
//import scala.annotation.StaticAnnotation

import java.util.{HashMap => HM}

trait Write[T] {
  def write(x: T): HM[String, Any]
}
