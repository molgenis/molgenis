package org.molgenis.data.meta;

import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.STRING;

import org.molgenis.data.repository.impl.AttributeMappingRepositoryImpl;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class EntityMappingMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "EntityMapping";
	public static final String IDENTIFIER = "identifier";
	public static final String SOURCEENTITYMETADATA = "sourceEntityMetaData";
	public static final String TARGETENTITYMETADATA = "targetEntityMetaData";
	public static final String ATTRIBUTEMAPPINGS = "attributeMappings";

	public EntityMappingMetaData()
	{
		super(ENTITY_NAME);

		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false).setDataType(STRING);
		addAttribute(SOURCEENTITYMETADATA);
		addAttribute(TARGETENTITYMETADATA);
		addAttribute(ATTRIBUTEMAPPINGS).setDataType(MREF).setRefEntity(AttributeMappingRepositoryImpl.META_DATA);
	}
}
