package org.molgenis.data;

import org.molgenis.data.meta.model.TagMetadata;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class UnknownTagException extends UnknownEntityException
{
	public UnknownTagException(TagMetadata tagMetadata, String tagId)
	{
		super(tagMetadata, tagId);
	}

	@Override
	public String getMessage()
	{
		return String.format("id:%s", getEntityId().toString());
	}
}

