package javalib.nio.channels

// Ported from Apache Harmony

import java.io.{File, FileOutputStream, IOException, RandomAccessFile}
import java.nio.channels.{ClosedChannelException, FileChannel, FileLock}

import org.junit.{Test, Before, After}
import org.junit.Assert._

import scalanative.junit.utils.AssertThrows.assertThrows

class FileLockTest {

  private var readWriteChannel: FileChannel = null
  private var mockLock: MockFileLock = null

  class MockFileLock(
      channel: FileChannel,
      position: Long,
      size: Long,
      shared: Boolean
  ) extends FileLock(channel, position, size, shared) {

    private var valid = true

    def isValid(): Boolean =
      valid

    def release() =
      valid = false
  }

  @Before def setUp(): Unit = {
    val tempFile = File.createTempFile("testing", "tmp")
    tempFile.deleteOnExit()
    val randomAccessFile = new RandomAccessFile(tempFile, "rw")
    readWriteChannel = randomAccessFile.getChannel()
    mockLock = new MockFileLock(readWriteChannel, 10, 100, false)
  }

  @After def tearDown(): Unit = {
    if (readWriteChannel != null) {
      readWriteChannel.close()
    }
  }

  @Test def testConstructorLjava_nio_channels_FileChannelJJZ(): Unit = {
    val fileLock1 = new MockFileLock(null, 0, 0, false)
    assertNull(fileLock1.channel())
    assertThrows(
      classOf[IllegalArgumentException],
      new MockFileLock(readWriteChannel, -1, 0, false)
    )
    assertThrows(
      classOf[IllegalArgumentException],
      new MockFileLock(readWriteChannel, 0, -1, false)
    )
  }

  @Test def testChannel(): Unit = {
    assertSame(readWriteChannel, mockLock.channel())
    val lock = new MockFileLock(null, 0, 10, true)
    assertNull(lock.channel())
  }

  @Test def testPosition(): Unit = {
    val fileLock1 = new MockFileLock(readWriteChannel, 20, 100, true)
    assertEquals(20, fileLock1.position())

    val position = Integer.MAX_VALUE.toLong + 1
    val fileLock2 = new MockFileLock(readWriteChannel, position, 100, true)
    assertEquals(position, fileLock2.position())
  }

  @Test def testSize(): Unit = {
    val fileLock1 = new MockFileLock(readWriteChannel, 20, 100, true)
    assertEquals(100, fileLock1.size())

    val position = 0x0fffffffffffffffL
    val size = Integer.MAX_VALUE.toLong + 1
    val fileLock2 = new MockFileLock(readWriteChannel, position, size, true)
    assertEquals(size, fileLock2.size())
  }

  @Test def testIsShared(): Unit = {
    assertFalse(mockLock.isShared())
    val lock = new MockFileLock(null, 0, 10, true)
    assertTrue(lock.isShared())
  }

  @Test def testOverlapsJJ(): Unit = {
    assertTrue("mockLock.overlaps(0, 11)", mockLock.overlaps(0, 11))
    assertFalse("mockLock.overlaps(0, 10)", mockLock.overlaps(0, 10))
    assertTrue("mockLock.overlaps(100, 110)", mockLock.overlaps(100, 110))
    assertTrue("mockLock.overlaps(99, 110)", mockLock.overlaps(99, 110))
    assertFalse("mockLock.overlaps(-1, 10)", mockLock.overlaps(-1, 10))
  }

  @Test def testIsValid(): Unit = {
    val fileLock = readWriteChannel.lock()
    assertTrue(fileLock.isValid())
    fileLock.release()
    assertFalse(fileLock.isValid())
  }

}
