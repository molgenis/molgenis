package org.molgenis.data.populate;

import com.google.common.io.BaseEncoding;
import org.molgenis.util.UniqueId;
import org.springframework.stereotype.Component;

/**
 * Generates a unique id. The generated ids can be used in urls and filenames.
 */
@Component
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