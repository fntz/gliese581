package com.github.fntz.gliese581

import java.util.{HashMap => HM}

import scala.collection.JavaConversions._

/*
 Source: http://stackoverflow.com/questions/31640565/converting-mapstring-any-to-a-case-class-using-shapeless
*/

import shapeless._
import shapeless.labelled.{FieldType, field}

trait Rethinkify {
  val id: Option[String]
}
// source http://stackoverflow.com/a/31638390/1581531
object Rethinkify {
  val defaultUUID = "id"
  implicit class ToMapRecOps[A](val a: A) extends AnyVal {
    def toMap[L <: HList](implicit
     gen: LabelledGeneric.Aux[A, L],
     tmr: ToMapRec[L]
    ): java.util.HashMap[String, Any] = {
      val h = new HM[String, Any]()
      tmr(gen.to(a)).foreach { case _ @ (k, v) =>
        v match {
          case None =>
          case _ => h.put(k, v)
        }
      }
      h
    }
  }
}

trait ToMapRec[L <: HList] { def apply(l: L): Map[String, Any] }

trait LowPriorityToMapRec {
  implicit def hconsToMapRec1[K <: Symbol, V, T <: HList](
     implicit wit: Witness.Aux[K], tmrT: ToMapRec[T]):
  ToMapRec[FieldType[K, V] :: T] = new ToMapRec[FieldType[K, V] :: T] {
    def apply(l: FieldType[K, V] :: T): Map[String, Any] =
      tmrT(l.tail) + (wit.value.name -> l.head)
  }
}

object ToMapRec extends LowPriorityToMapRec {
  implicit val hnilToMapRec: ToMapRec[HNil] = new ToMapRec[HNil] {
    def apply(l: HNil): Map[String, Any] = Map.empty
  }

  implicit def hconsToMapRec0[K <: Symbol, V, R <: HList, T <: HList](
     implicit wit: Witness.Aux[K], gen: LabelledGeneric.Aux[V, R],
    tmrH: ToMapRec[R], tmrT: ToMapRec[T]): ToMapRec[FieldType[K, V] :: T] = new ToMapRec[FieldType[K, V] :: T] {
    def apply(l: FieldType[K, V] :: T): Map[String, Any] =
      tmrT(l.tail) + (wit.value.name -> tmrH(gen.to(l.head)))
  }
}


trait FromMap[T <: HList] {
  def apply(hm: Map[String, Any]): Option[T]
}

trait LowPriorityFromMap {
  implicit def hconsFromMap[K <: Symbol, V, T <: HList](
    implicit witness: Witness.Aux[K],
    typeable: Typeable[V],
    fromMapT: FromMap[T]
  ): FromMap[FieldType[K, V] :: T] = new FromMap[FieldType[K, V]::T] {
    def apply(m: Map[String, Any]): Option[FieldType[K, V]::T] = {
      val n = witness.value.name
      val x = m.get(n)
      val v = if (n == Rethinkify.defaultUUID) {
        Option(x)
      } else {
        x
      }

      for {
        r <- v
        h <- typeable.cast(r)
        t <- fromMapT(m)
      } yield {
        field[K](h)::t
      }
    }
  }
}

object FromMap extends LowPriorityFromMap {
  implicit val hnilFromMap: FromMap[HNil] = new FromMap[HNil] {
    override def apply(m: Map[String, Any]): Option[HNil] = Some(HNil)
  }
  implicit def hconsFromMap0[K <: Symbol, V, R <: HList, T <: HList](
    implicit witness: Witness.Aux[K],
    gen: LabelledGeneric.Aux[V, R],
    fromMapH: FromMap[R],
    fromMapT: FromMap[T]
  ): FromMap[FieldType[K, V]::T ] = new FromMap[::[FieldType[K, V], T]] {
    override def apply(hm: Map[String, Any]): Option[::[FieldType[K, V], T]] = {
      for {
        v <- hm.get(witness.value.name)
        r <- Typeable[Map[String, Any]].cast(v)
        h <- fromMapH(r)
        t <- fromMapT(hm)
      } yield {
        field[K](gen.from(h))::t
      }
    }
  }
}

class RethinkTransformer[A] {
  def from[R <: HList](m: HM[String, Any])(
    implicit gen: LabelledGeneric.Aux[A, R],
      fromMap: FromMap[R]): Option[A] = fromMap(m.toMap).map(gen.from)
}

object RethinkTransformer {
  def to[T]: RethinkTransformer[T] = new RethinkTransformer[T]
}
