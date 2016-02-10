package org.molgenis.data.mapper.meta;

import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.data.mapper.repository.impl.AttributeMappingRepositoryImpl;
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

		addAttribute(IDENTIFIER, ROLE_ID);
		addAttribute(SOURCEENTITYMETADATA);
		addAttribute(TARGETENTITYMETADATA);
		addAttribute(ATTRIBUTEMAPPINGS).setDataType(MREF).setRefEntity(AttributeMappingRepositoryImpl.META_DATA);
	}
}
