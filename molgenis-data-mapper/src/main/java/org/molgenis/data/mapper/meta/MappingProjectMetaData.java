package org.molgenis.data.mapper.meta;

import org.molgenis.auth.MolgenisUserMetaData;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.AttributeType.MREF;
import static org.molgenis.MolgenisFieldTypes.AttributeType.XREF;
import static org.molgenis.data.mapper.meta.MapperPackage.PACKAGE_MAPPER;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class MappingProjectMetaData extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "MappingProject";
	public static final String MAPPING_PROJECT = PACKAGE_MAPPER + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String IDENTIFIER = "identifier";
	public static final String NAME = "name";
	public static final String OWNER = "owner";
	public static final String MAPPING_TARGETS = "mappingtargets";

	private final MapperPackage mapperPackage;
	private final MolgenisUserMetaData molgenisUserMetaData;
	private final MappingTargetMetaData mappingTargetMetaData;

	@Autowired
	public MappingProjectMetaData(MapperPackage mapperPackage, MolgenisUserMetaData molgenisUserMetaData,
			MappingTargetMetaData mappingTargetMetaData)
	{
		super(SIMPLE_NAME, PACKAGE_MAPPER);
		this.mapperPackage = requireNonNull(mapperPackage);
		this.molgenisUserMetaData = requireNonNull(molgenisUserMetaData);
		this.mappingTargetMetaData = requireNonNull(mappingTargetMetaData);
	}

	@Override
	public void init()
	{
		setPackage(mapperPackage);

		addAttribute(IDENTIFIER, ROLE_ID);
		addAttribute(NAME).setNillable(false);
		addAttribute(OWNER).setDataType(XREF).setRefEntity(molgenisUserMetaData);
		addAttribute(MAPPING_TARGETS).setDataType(MREF).setRefEntity(mappingTargetMetaData);
	}
}
