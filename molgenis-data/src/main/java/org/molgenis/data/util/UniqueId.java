package org.molgenis.data.util;

import com.eaio.uuid.UUIDGen;

import java.nio.ByteBuffer;

/**
 * Copied from https://github.com/mumrah/flake-java/blob/master/src/uniq/UniqueId.java
 * <p>
 * See also http://boundary.com/blog/2012/01/12/flake-a-decentralized-k-ordered-unique-id-generator-in-erlang/
 */
public class UniqueId
{

	// Get the MAC address (i.e., the "node" from a UUID1)
	private final long clockSeqAndNode = UUIDGen.getClockSeqAndNode();
	private final byte[] node = new byte[] { (byte) ((clockSeqAndNode >> 40) & 0xff),
			(byte) ((clockSeqAndNode >> 32) & 0xff), (byte) ((clockSeqAndNode >> 24) & 0xff),
			(byte) ((clockSeqAndNode >> 16) & 0xff), (byte) ((clockSeqAndNode >> 8) & 0xff),
			(byte) ((clockSeqAndNode) & 0xff), };
	private final ThreadLocal<ByteBuffer> tlbb = ThreadLocal.withInitial(() -> ByteBuffer.allocate(16));

	private volatile int seq;
	private volatile long lastTimestamp;
	private final Object lock = new Object();

	private final int maxShort = 0xffff;

	public byte[] getId()
	{
		if (seq == maxShort)
		{
			throw new RuntimeException("Too fast");
		}

		long time;
		synchronized (lock)
		{
			time = System.currentTimeMillis();
			if (time != lastTimestamp)
			{
				lastTimestamp = time;
				seq = 0;
			}
			seq++;
			ByteBuffer bb = tlbb.get();
			bb.rewind();
			bb.putLong(time);
			bb.put(node);
			bb.putShort((short) seq);
			return bb.array();
		}
	}
}