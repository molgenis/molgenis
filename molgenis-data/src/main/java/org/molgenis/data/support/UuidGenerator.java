package org.molgenis.data.support;

import org.molgenis.data.IdGenerator;
import org.molgenis.util.UniqueId;

/**
 * Generates a unique id. The generated ids can be used in urls and filenames.
 */
public class UuidGenerator implements IdGenerator
{
	private final UniqueId uniqueId;

	public UuidGenerator()
	{
		uniqueId = new UniqueId();
	}

	@Override
	public String generateId()
	{
		return uniqueId.getStringId();
	}
}