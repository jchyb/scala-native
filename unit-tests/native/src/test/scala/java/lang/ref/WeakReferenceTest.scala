package java.lang.ref

import org.junit.Test
import org.junit.Assert._
import org.junit.Assume._

import scala.scalanative.meta.LinktimeInfo.isWeakReferenceSupported
import scala.scalanative.annotation.nooptimize

// "AfterGC" tests are very sensitive to optimizations,
// both by Scala Native and LLVM.
class WeakReferenceTest {

  case class A()

  def gcAssumption(): Unit = {
    assumeTrue(
      "WeakReferences work only on Commix and Immix GC",
      isWeakReferenceSupported
    )
  }

  @noinline def allocWeakRef(
      referenceQueue: ReferenceQueue[A]
  ): WeakReference[A] = {
    var a = A()
    val weakRef = new WeakReference(a, referenceQueue)
    assertEquals("get() should return object reference", weakRef.get(), A())
    a = null
    weakRef
  }

  @nooptimize @Test def addsToReferenceQueueAfterGC(): Unit = {
    gcAssumption()
    val refQueue = new ReferenceQueue[A]()
    val weakRef1 = allocWeakRef(refQueue)
    val weakRef2 = allocWeakRef(refQueue)
    val weakRefList = List(weakRef1, weakRef2)

    // limit - arbitrary number that can be higher or lower,
    // depending on how much garbage is necessary to perform GC
    val limit = 1000000
    for (i <- 0 to limit) {

      // Allocation spamming. GC.collect() proved to be unreliable
      // for commix (as in issue #2367), so we force garbage collection
      // to happen organically.
      A().toString()

      // We do not want to put the reference on stack
      // during GC, so we hide it behind an if block
      if (i == limit) {
        assertEquals(weakRef1.get(), null)
        assertEquals(weakRef2.get(), null)
        val a = refQueue.poll()
        val b = refQueue.poll()
        assertTrue(weakRefList.contains(a))
        assertTrue(weakRefList.contains(b))
        assertNotEquals(a, b)
        assertEquals(refQueue.poll(), null)
      }
    }
  }

  @Test def clear(): Unit = {
    val refQueue = new ReferenceQueue[A]()
    val a = A()
    val weakRef = new WeakReference(a, refQueue)

    assertEquals(refQueue.poll(), null)

    weakRef.clear()
    assertEquals(weakRef.get(), null)
    assertEquals(refQueue.poll(), weakRef)
    assertEquals(refQueue.poll(), null)
  }

  @Test def enqueue(): Unit = {
    val refQueue = new ReferenceQueue[A]()
    val a = A()
    val weakRef = new WeakReference(a, refQueue)

    assertEquals(refQueue.poll(), null)

    weakRef.enqueue()
    assertEquals(weakRef.get(), a)
    assertEquals(refQueue.poll(), weakRef)
    assertEquals(refQueue.poll(), null)
  }

}
