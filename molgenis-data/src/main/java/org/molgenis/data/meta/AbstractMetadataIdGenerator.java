package org.molgenis.data.meta;

import com.google.common.hash.Hashing;

import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class AbstractMetadataIdGenerator implements MetadataIdGenerator
{
	/**
	 * Generates a eight character [a-z0-9] system unique identifier.
	 *
	 * @param id identifier of variable length
	 * @return hashcode
	 */
	protected String generateHashcode(Object id)
	{
		return Hashing.crc32().hashString(id.toString(), UTF_8).toString();
	}
}
