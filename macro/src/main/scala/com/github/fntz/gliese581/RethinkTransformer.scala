package com.github.fntz.gliese581

import java.lang.{Long => L}
import java.util.{HashMap => HM}

import shapeless.Typeable.ValueTypeable

import scala.collection.JavaConversions._

/*
 Source: http://stackoverflow.com/questions/31640565/converting-mapstring-any-to-a-case-class-using-shapeless
*/

import shapeless._
import labelled.{FieldType, field}

trait FromMap[T <: HList] {
  def apply(hm: Map[String, Any]): Option[T]
}

trait LowPriorityFromMap {
  implicit val jllongTypeable: Typeable[L] =
    ValueTypeable[L, L](classOf[L], "Long")


  implicit def hconsFromMap[K <: Symbol, V, T <: HList](
    implicit witness: Witness.Aux[K],
    typeable: Typeable[V],
    fromMapT: FromMap[T]
  ): FromMap[FieldType[K, V] :: T] = new FromMap[FieldType[K, V]::T] {
    def apply(m: Map[String, Any]): Option[FieldType[K, V]::T] = {
      val n = witness.value.name
      val x = m.get(n)
      val v = if (n == "id") {
        Option(x)
      } else {
        x
      }
      if (n == "age") {
        println("@"*100)
        println(typeable.cast(10))
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

class ConvertHelper[A] {
  def from[R <: HList](m: HM[String, Any])(
    implicit gen: LabelledGeneric.Aux[A, R],
      fromMap: FromMap[R]): Option[A] = fromMap(m.toMap).map(gen.from)
}

object RethinkTransformer {
  def to[T]: ConvertHelper[T] = new ConvertHelper[T]
}
