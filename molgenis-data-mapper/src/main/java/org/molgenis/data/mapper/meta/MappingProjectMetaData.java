package org.molgenis.data.mapper.meta;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.mapper.meta.MapperPackage.PACKAGE_MAPPER;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.Package.PACKAGE_SEPARATOR;

import org.molgenis.auth.MolgenisUserMetaData;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MappingProjectMetaData extends SystemEntityMetaDataImpl
{
	public static final String SIMPLE_NAME = "MappingProject";
	public static final String MAPPING_PROJECT = PACKAGE_MAPPER + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String IDENTIFIER = "identifier";
	public static final String NAME = "name";
	public static final String OWNER = "owner";
	public static final String MAPPINGTARGETS = "mappingtargets";

	private final MapperPackage mapperPackage;
	private MolgenisUserMetaData molgenisUserMetaData;
	private MappingTargetMetaData mappingTargetMetaData;

	@Autowired
	public MappingProjectMetaData(MapperPackage mapperPackage)
	{
		super(SIMPLE_NAME, PACKAGE_MAPPER);
		this.mapperPackage = requireNonNull(mapperPackage);
	}

	@Override
	public void init()
	{
		setPackage(mapperPackage);

		addAttribute(IDENTIFIER, ROLE_ID);
		addAttribute(NAME).setNillable(false);
		addAttribute(OWNER).setDataType(XREF).setRefEntity(molgenisUserMetaData);
		addAttribute(MAPPINGTARGETS).setDataType(MREF).setRefEntity(mappingTargetMetaData);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setMolgenisUserMetaData(MolgenisUserMetaData molgenisUserMetaData)
	{
		this.molgenisUserMetaData = requireNonNull(molgenisUserMetaData);
	}

	@Autowired
	public void setMappingTargetMetaData(MappingTargetMetaData mappingTargetMetaData)
	{
		this.mappingTargetMetaData = requireNonNull(mappingTargetMetaData);
	}
}
