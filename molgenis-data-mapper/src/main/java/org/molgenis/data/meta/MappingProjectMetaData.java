package org.molgenis.data.meta;

import org.molgenis.data.repository.impl.EntityMappingRepositoryImpl;
import org.molgenis.data.support.DefaultEntityMetaData;

import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.MolgenisFieldTypes.MREF;

public class MappingProjectMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "MappingProject";
	public static final String IDENTIFIER = "identifier";
	public static final String OWNER = "owner";
	public static final String ENTITYMAPPINGS = "entityMappings";

	public MappingProjectMetaData()
	{
		super(ENTITY_NAME);

		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false).setDataType(STRING);
		// FIXME use xref to MolgenisUser when https://github.com/molgenis/molgenis/issues/2054 is fixed
		addAttribute(OWNER).setDataType(STRING);
		addAttribute(ENTITYMAPPINGS).setDataType(MREF).setRefEntity(EntityMappingRepositoryImpl.META_DATA);
	}
}
