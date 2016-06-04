package org.molgenis.security.core;

import com.google.common.io.BaseEncoding;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Generates secure IDs.
 * <p>
 * The IDs should be hard to guess, so therefore cannot be in order like the normal IdGenerator makes them.
 */
@Component
public class SecureIdGeneratorImpl implements SecureIdGenerator
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

	@Override
	public String generatePassword()
	{
		return generateActivationCode().substring(0, 8);
	}

	@Override
	public String generateActivationCode()
	{
		return UUID.randomUUID().toString();
	}
}
