package org.molgenis.data.mapper.meta;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.MREF;
import static org.molgenis.data.mapper.meta.MapperPackage.PACKAGE_MAPPER;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class MappingTargetMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "MappingTarget";
	public static final String MAPPING_TARGET = PACKAGE_MAPPER + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String IDENTIFIER = "identifier";
	public static final String ENTITY_MAPPINGS = "entityMappings";
	public static final String TARGET = "target";

	private final MapperPackage mapperPackage;
	private final EntityMappingMetaData entityMappingMetaData;

	@Autowired
	public MappingTargetMetaData(MapperPackage mapperPackage, EntityMappingMetaData entityMappingMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_MAPPER);
		this.mapperPackage = requireNonNull(mapperPackage);
		this.entityMappingMetaData = requireNonNull(entityMappingMetaData);
	}

	@Override
	public void init()
	{
		setLabel("Mapping target");
		setPackage(mapperPackage);

		addAttribute(IDENTIFIER, ROLE_ID);
		addAttribute(ENTITY_MAPPINGS).setDataType(MREF).setRefEntity(entityMappingMetaData);
		addAttribute(TARGET).setNillable(false);
	}
}
