package org.molgenis.data.meta;

import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.STRING;

import org.molgenis.data.repository.impl.EntityMappingRepositoryImpl;
import org.molgenis.data.support.DefaultEntityMetaData;

public class MappingTargetMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "MappingTarget";
	public static final String IDENTIFIER = "identifier";
	public static final String ENTITYMAPPINGS = "entityMappings";
	public static final String TARGET = "target";

	public MappingTargetMetaData()
	{
		super(ENTITY_NAME);
		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false).setDataType(STRING);
		addAttribute(ENTITYMAPPINGS).setDataType(MREF).setRefEntity(EntityMappingRepositoryImpl.META_DATA);
		addAttribute(TARGET).setNillable(false);
	}
}
