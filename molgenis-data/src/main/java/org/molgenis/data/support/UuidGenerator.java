package org.molgenis.data.support;

import org.molgenis.data.IdGenerator;
import org.molgenis.util.UniqueId;

import com.google.common.io.BaseEncoding;

/**
 * Generates a unique id. The generated ids can be used in urls and filenames.
 */
public class UuidGenerator implements IdGenerator
{
	private final BaseEncoding baseEncoding;
	private final UniqueId uniqueId;

	public UuidGenerator()
	{
		baseEncoding = BaseEncoding.base32().omitPadding();
		uniqueId = new UniqueId();
	}

	@Override
	public String generateId()
	{
		byte[] uuidBytes = uniqueId.getId();
		return baseEncoding.encode(uuidBytes);
	}
}