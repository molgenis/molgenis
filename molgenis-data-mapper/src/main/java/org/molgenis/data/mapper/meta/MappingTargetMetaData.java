package org.molgenis.data.mapper.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.auth.AuthorityMetaData;
import org.molgenis.data.mapper.repository.impl.EntityMappingRepositoryImpl;
import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MappingTargetMetaData extends SystemEntityMetaDataImpl
{
	public static final String ENTITY_NAME = "MappingTarget";
	public static final String IDENTIFIER = "identifier";
	public static final String ENTITYMAPPINGS = "entityMappings";
	public static final String TARGET = "target";

	private EntityMappingMetaData entityMappingMetaData;

	@Override
	public void init()
	{
		setName(ENTITY_NAME);
		addAttribute(IDENTIFIER, ROLE_ID);
		addAttribute(ENTITYMAPPINGS).setDataType(MREF).setRefEntity(entityMappingMetaData);
		addAttribute(TARGET).setNillable(false);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setEntityMappingMetaData(EntityMappingMetaData entityMappingMetaData)
	{
		this.entityMappingMetaData = requireNonNull(entityMappingMetaData);
	}
}
