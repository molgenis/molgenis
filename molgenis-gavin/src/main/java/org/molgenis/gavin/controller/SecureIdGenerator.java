package org.molgenis.gavin.controller;

import com.google.common.io.BaseEncoding;
import org.molgenis.data.populate.IdGenerator;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Generates secure IDs.
 * <p>
 * The IDs should be hard to guess, so therefore cannot be in order like the normal {@link org.molgenis.data.populate.UuidGenerator} makes them.
 */
class SecureIdGenerator implements IdGenerator
{
	private final BaseEncoding baseEncoding = BaseEncoding.base32().omitPadding();

	@Override
	public String generateId()
	{
		UUID uuid = UUID.randomUUID();
		ByteBuffer buffer = ByteBuffer.allocate(2 * Long.BYTES);
		buffer.putLong(uuid.getMostSignificantBits());
		buffer.putLong(uuid.getLeastSignificantBits());
		return baseEncoding.encode(buffer.array());
	}
}
