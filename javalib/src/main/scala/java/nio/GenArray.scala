package java.nio
import scala.scalanative.unsafe._
import scala.scalanative.runtime.ByteArray
import scala.scalanative.libc.string
import scala.scalanative.runtime
import scala.scalanative.unsigned._

private[nio] sealed trait GenArray[T] {
  def update(index: Int, value: T): Unit
  def apply(index: Int): T
  def toArray(): Array[T]
  def toPtr(): Ptr[Byte]
  val length: Int
}

private[nio] case class ScalaArray[T](array: Array[T]) extends GenArray[T] {

  @inline override def update(index: Int, value: T): Unit =
    array(index) = value

  @inline override def apply(index: Int): T =
    array(index)

  @inline override def toArray(): Array[T] =
    array

  @inline override def toPtr(): Ptr[Byte] =
    array.asInstanceOf[ByteArray].at(0)

  val length = array.length
}

private[nio] case class PtrArray(array: Ptr[Byte], val length: Int)
    extends GenArray[Byte] {

  @inline override def update(index: Int, value: Byte): Unit =
    array(index) = value

  @inline override def apply(index: Int): Byte =
    array(index)

  @inline override def toArray(): Array[Byte] = {
    val a = Array.ofDim[Byte](length)
    string.memcpy(a.asInstanceOf[runtime.ByteArray].at(0), array, length.toUInt)
    a
  }

  @inline override def toPtr(): Ptr[Byte] =
    array
}
