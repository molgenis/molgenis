package org.molgenis.auth;

import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LABEL;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class GroupAuthorityMetaData extends DefaultEntityMetaData
{

	public static final String ENTITY_NAME = "GroupAuthority";

	public GroupAuthorityMetaData()
	{
		super(ENTITY_NAME);
		addAttribute(GroupAuthority.ID, ROLE_ID, ROLE_LABEL).setAuto(true).setVisible(false).setDescription("")
				.setNillable(false);
		addAttribute(GroupAuthority.MOLGENISGROUP).setDataType(XREF).setRefEntity(new MolgenisGroupMetaData())
				.setAggregateable(true).setDescription("").setNillable(false);
		addAttribute(GroupAuthority.ROLE).setDescription("").setNillable(false);
	}
}
