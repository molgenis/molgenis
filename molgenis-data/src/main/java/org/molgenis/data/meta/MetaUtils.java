package org.molgenis.data.meta;

import org.molgenis.data.Fetch;

import static org.molgenis.data.meta.model.EntityTypeMetadata.*;

public class MetaUtils
{
	public static Fetch getEntityTypeFetch()
	{
		// TODO simplify fetch creation (in this case *all* attributes and expand xref/mrefs)
		return new Fetch().field(FULL_NAME).field(SIMPLE_NAME).field(PACKAGE).field(LABEL).field(DESCRIPTION)
				.field(ATTRIBUTES).field(IS_ABSTRACT).field(EXTENDS).field(TAGS).field(BACKEND);
	}
}
