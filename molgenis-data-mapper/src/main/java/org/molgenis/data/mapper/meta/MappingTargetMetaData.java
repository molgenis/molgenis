package org.molgenis.data.mapper.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.data.mapper.meta.MapperPackage.PACKAGE_MAPPER;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.Package.PACKAGE_SEPARATOR;

import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MappingTargetMetaData extends SystemEntityMetaDataImpl
{
	private static final String SIMPLE_NAME = "MappingTarget";
	public static final String MAPPING_TARGET = PACKAGE_MAPPER + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String IDENTIFIER = "identifier";
	public static final String ENTITYMAPPINGS = "entityMappings";
	public static final String TARGET = "target";
	private final MapperPackage mapperPackage;

	private EntityMappingMetaData entityMappingMetaData;

	@Autowired
	public MappingTargetMetaData(MapperPackage mapperPackage)
	{
		super(SIMPLE_NAME, PACKAGE_MAPPER);
		this.mapperPackage = requireNonNull(mapperPackage);
	}

	@Override
	public void init()
	{
		setPackage(mapperPackage);

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
