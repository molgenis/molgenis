package org.molgenis.data.meta;

import com.google.common.hash.Hashing;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Generator for human readable entity type and attribute identifiers.
 */
public interface MetadataIdGenerator<S, T>
{
	/**
	 * Generates a human readable identifier for the given entity type.
	 *
	 * @param entityIdentity entity type ID
	 * @return human readable entity type identifier
	 */
	String generateEntityTypeId(S entityIdentity);

	/**
	 * Generates a human readable identifier for the given attribute.
	 *
	 * @param attributeIdentity attribute I
	 *
	 * @return human readable attribute identifier
	 */
	String generateAttributeId(T attributeIdentity);

	default String generateHashcode(Object id)
	{
		return Hashing.crc32().hashString(id.toString(), UTF_8).toString();
	}
}
