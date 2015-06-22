package org.molgenis.data.mapper.meta;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class AttributeMappingMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "AttributeMapping";
	public static final String IDENTIFIER = "identifier";
	public static final String TARGETATTRIBUTEMETADATA = "targetAttributeMetaData";
	public static final String SOURCEATTRIBUTEMETADATAS = "sourceAttributeMetaDatas";
	public static final String ALGORITHM = "algorithm";

	public AttributeMappingMetaData()
	{
		super(ENTITY_NAME);

		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false);
		addAttribute(TARGETATTRIBUTEMETADATA).setNillable(false);
		addAttribute(SOURCEATTRIBUTEMETADATAS);
		addAttribute(ALGORITHM);
	}
}
