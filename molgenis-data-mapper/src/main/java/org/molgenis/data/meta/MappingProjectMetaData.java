package org.molgenis.data.meta;

import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.STRING;

import org.molgenis.data.repository.impl.MappingTargetRepositoryImpl;
import org.molgenis.data.support.DefaultEntityMetaData;

public class MappingProjectMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "MappingProject";
	public static final String IDENTIFIER = "identifier";
	public static final String NAME = "name";
	public static final String OWNER = "owner";
	public static final String MAPPINGTARGETS = "mappingtargets";

	public MappingProjectMetaData()
	{
		super(ENTITY_NAME);

		addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false).setDataType(STRING);
		addAttribute(NAME).setNillable(false);
		// FIXME use xref to MolgenisUser when https://github.com/molgenis/molgenis/issues/2054 is fixed
		addAttribute(OWNER).setDataType(STRING);
		addAttribute(MAPPINGTARGETS).setDataType(MREF).setRefEntity(MappingTargetRepositoryImpl.META_DATA);
	}
}
