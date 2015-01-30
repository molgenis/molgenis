package org.molgenis.data.meta;

import org.molgenis.data.support.DefaultEntityMetaData;

public class AttributeMappingMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "AttributeMapping";
	public static final String IDENTIFIER = "identifier";
	public static final String SOURCEATTRIBUTEMETADATA = "sourceAttributeMetaData";
	public static final String TARGETATTRIBUTEMETADATA = "targetAttributeMetaData";
	public static final String ALGORITHM = "algorithm";

	public AttributeMappingMetaData()
	{
		super(ENTITY_NAME);

		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false);
		addAttribute(SOURCEATTRIBUTEMETADATA).setNillable(false);
		addAttribute(TARGETATTRIBUTEMETADATA).setNillable(false);
		addAttribute(ALGORITHM);
	}
}
