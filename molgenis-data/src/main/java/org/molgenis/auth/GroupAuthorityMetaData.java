package org.molgenis.auth;

import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.springframework.stereotype.Component;

@Component
public class GroupAuthorityMetaData extends SystemEntityMetaDataImpl
{
	public static final String ENTITY_NAME = "GroupAuthority";

	@Override
	public void init()
	{
		setName(ENTITY_NAME);
		addAttribute(GroupAuthority.ID, ROLE_ID).setAuto(true).setVisible(false).setDescription("");
		addAttribute(GroupAuthority.MOLGENISGROUP).setDataType(XREF).setRefEntity(new MolgenisGroupMetaData())
				.setAggregatable(true).setDescription("").setNillable(false);
		addAttribute(GroupAuthority.ROLE).setDescription("").setNillable(false);
	}
}
