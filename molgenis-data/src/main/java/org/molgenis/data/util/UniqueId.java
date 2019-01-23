package org.molgenis.data.util;

import com.eaio.uuid.UUIDGen;
import java.nio.ByteBuffer;

/**
 * Copied from https://github.com/mumrah/flake-java/blob/master/src/uniq/UniqueId.java
 *
 * <p>See also
 * http://boundary.com/blog/2012/01/12/flake-a-decentralized-k-ordered-unique-id-generator-in-erlang/
 */
public class UniqueId {

  // Get the MAC address (i.e., the "node" from a UUID1)
  private static final long CLOCK_SEQ_AND_NODE = UUIDGen.getClockSeqAndNode();
  private static final byte[] NODE =
      new byte[] {
        (byte) ((CLOCK_SEQ_AND_NODE >> 40) & 0xff),
        (byte) ((CLOCK_SEQ_AND_NODE >> 32) & 0xff),
        (byte) ((CLOCK_SEQ_AND_NODE >> 24) & 0xff),
        (byte) ((CLOCK_SEQ_AND_NODE >> 16) & 0xff),
        (byte) ((CLOCK_SEQ_AND_NODE >> 8) & 0xff),
        (byte) ((CLOCK_SEQ_AND_NODE) & 0xff),
      };
  private final ThreadLocal<ByteBuffer> tlbb =
      ThreadLocal.withInitial(() -> ByteBuffer.allocate(16));

  private volatile int seq;
  private volatile long lastTimestamp;
  private final Object lock = new Object();

  private static final int MAX_SHORT = 0xffff;

  public byte[] getId() {
    if (seq == MAX_SHORT) {
      throw new RuntimeException("Too fast");
    }

    long time;
    synchronized (lock) {
      time = System.currentTimeMillis();
      if (time != lastTimestamp) {
        lastTimestamp = time;
        seq = 0;
      }
      seq++;
      ByteBuffer bb = tlbb.get();
      bb.rewind();
      bb.putLong(time);
      bb.put(NODE);
      bb.putShort((short) seq);
      return bb.array();
    }
  }
}
