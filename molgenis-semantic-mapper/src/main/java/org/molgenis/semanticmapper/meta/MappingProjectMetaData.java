package org.molgenis.semanticmapper.meta;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.semanticmapper.meta.MapperPackage.PACKAGE_MAPPER;

@Component
public class MappingProjectMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "MappingProject";
	public static final String MAPPING_PROJECT = PACKAGE_MAPPER + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String IDENTIFIER = "identifier";
	public static final String NAME = "name";
	public static final String MAPPING_TARGETS = "mappingtargets";

	private final MapperPackage mapperPackage;
	private final MappingTargetMetaData mappingTargetMetaData;

	public MappingProjectMetaData(MapperPackage mapperPackage, MappingTargetMetaData mappingTargetMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_MAPPER);
		this.mapperPackage = requireNonNull(mapperPackage);
		this.mappingTargetMetaData = requireNonNull(mappingTargetMetaData);
	}

	@Override
	public void init()
	{
		setLabel("Mapping project");
		setPackage(mapperPackage);

		addAttribute(IDENTIFIER, ROLE_ID);
		addAttribute(NAME).setNillable(false);
		addAttribute(MAPPING_TARGETS).setDataType(MREF).setRefEntity(mappingTargetMetaData).setCascadeDelete(true);
	}
}
