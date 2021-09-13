package java.lang.ref

import scalanative.annotation.stub
import scala.collection.mutable

class ReferenceQueue[T >: Null <: AnyRef] {
  private val underlying = mutable.Queue[Reference[_]]()
  private[java] def add(reference: Reference[_]): Unit =
    underlying += reference

  def poll(): java.lang.ref.Reference[_] =
    underlying.headOption.getOrElse(null)

  @stub
  def remove(): java.lang.ref.Reference[_] = ???

  @stub
  def remove(timeOut: Long): java.lang.ref.Reference[_] = ???
}
