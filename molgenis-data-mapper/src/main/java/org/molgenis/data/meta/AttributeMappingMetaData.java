package org.molgenis.data.meta;

import org.molgenis.data.support.DefaultEntityMetaData;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.MolgenisFieldTypes.XREF;

public class AttributeMappingMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "attributes";
	public static final String IDENTIFIER = "identifier";
	public static final String SOURCEATTRIBUTEMETADATA = "sourceAttribureMetaData";
	public static final String TARGETATTRIBUTEMETADATA = "targetAttribureMetaData";
	public static final String ALGORITHM = "algorithm";

	public AttributeMappingMetaData()
	{
		super(ENTITY_NAME);

		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false).setDataType(STRING);
		addAttribute(SOURCEATTRIBUTEMETADATA).setDataType(XREF).setRefEntity(AttributeMetaDataRepository.META_DATA);
		addAttribute(TARGETATTRIBUTEMETADATA).setDataType(XREF).setRefEntity(AttributeMetaDataRepository.META_DATA);
		addAttribute(ALGORITHM).setDataType(STRING);
	}
}
