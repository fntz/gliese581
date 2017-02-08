package com.github.fntz.gliese581.util

import java.util.{HashMap => HM}

object HashMapImplicits {
  implicit class HMExtension[K, V](hm: HM[K, V]) {
    def optGet[N](key: String): Option[N] = {
      val r = hm.get(key)

      if (r == null) {
        None
      } else {
        Some(r.asInstanceOf[N])
      }
    }

    def getAs[N](key: String): N = {
      val r = hm.get(key)
      // null!
      r.asInstanceOf[N]

    }
  }
}
