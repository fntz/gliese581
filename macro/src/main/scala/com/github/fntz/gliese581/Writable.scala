package com.github.fntz.gliese581


trait Writable[T] { self: TypeSafeTable[T] =>

  def insert(x: T) = ???
  def update(x: T) = ???
  def replace(x: T) = ???
  def delete(x: T) = ???
  def sync(x: T) = ???

}
