package org.molgenis.data.mapper.meta;

import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.auth.MolgenisUserMetaData;
import org.molgenis.data.mapper.repository.impl.MappingTargetRepositoryImpl;
import org.molgenis.data.meta.EntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class MappingProjectMetaData extends EntityMetaData
{
	public static final String ENTITY_NAME = "MappingProject";
	public static final String IDENTIFIER = "identifier";
	public static final String NAME = "name";
	public static final String OWNER = "owner";
	public static final String MAPPINGTARGETS = "mappingtargets";

	public MappingProjectMetaData()
	{
		super(ENTITY_NAME);

		addAttribute(IDENTIFIER, ROLE_ID);
		addAttribute(NAME).setNillable(false);
		addAttribute(OWNER).setDataType(XREF).setRefEntity(new MolgenisUserMetaData());
		addAttribute(MAPPINGTARGETS).setDataType(MREF).setRefEntity(MappingTargetRepositoryImpl.META_DATA);
	}
}
