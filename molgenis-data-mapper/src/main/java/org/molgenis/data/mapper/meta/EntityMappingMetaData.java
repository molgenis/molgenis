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
public class EntityMappingMetaData extends SystemEntityMetaDataImpl
{
	public static final String SIMPLE_NAME = "EntityMapping";
	public static final String ENTITY_MAPPING = PACKAGE_MAPPER + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String IDENTIFIER = "identifier";
	public static final String SOURCEENTITYMETADATA = "sourceEntityMetaData";
	public static final String TARGETENTITYMETADATA = "targetEntityMetaData";
	public static final String ATTRIBUTEMAPPINGS = "attributeMappings";

	private final MapperPackage mapperPackage;
	private AttributeMappingMetaData attributeMappingMetaData;

	@Autowired
	public EntityMappingMetaData(MapperPackage mapperPackage)
	{
		super(SIMPLE_NAME, PACKAGE_MAPPER);
		this.mapperPackage = requireNonNull(mapperPackage);
	}

	@Override
	public void init()
	{
		setPackage(mapperPackage);

		addAttribute(IDENTIFIER, ROLE_ID);
		addAttribute(SOURCEENTITYMETADATA);
		addAttribute(TARGETENTITYMETADATA);
		addAttribute(ATTRIBUTEMAPPINGS).setDataType(MREF).setRefEntity(attributeMappingMetaData);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setAttributeMappingMetaData(AttributeMappingMetaData attributeMappingMetaData)
	{
		this.attributeMappingMetaData = requireNonNull(attributeMappingMetaData);
	}
}
