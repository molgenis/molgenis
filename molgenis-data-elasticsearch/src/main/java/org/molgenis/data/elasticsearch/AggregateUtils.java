package org.molgenis.data.elasticsearch;

import org.molgenis.data.meta.model.Attribute;

import static org.molgenis.data.support.EntityTypeUtils.isReferenceType;

public class AggregateUtils
{
	private AggregateUtils()
	{
	}

	public static boolean isNestedType(Attribute attr)
	{
		return isReferenceType(attr);
	}
}
