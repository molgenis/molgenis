package org.molgenis.data.meta;

import org.molgenis.data.Fetch;

import static org.molgenis.data.meta.model.EntityMetaDataMetaData.*;

public class MetaUtils
{
	public static Fetch getEntityMetaDataFetch()
	{
		// TODO simplify fetch creation (in this case *all* attributes and expand xref/mrefs)
		return new Fetch().field(FULL_NAME).field(SIMPLE_NAME).field(PACKAGE).field(LABEL).field(DESCRIPTION)
				.field(ATTRIBUTES).field(ID_ATTRIBUTE).field(LABEL_ATTRIBUTE).field(LOOKUP_ATTRIBUTES).field(ABSTRACT)
				.field(EXTENDS).field(TAGS).field(BACKEND);
	}
}
