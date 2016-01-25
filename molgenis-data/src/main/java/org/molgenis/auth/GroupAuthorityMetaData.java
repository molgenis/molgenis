package org.molgenis.auth;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

import static org.molgenis.MolgenisFieldTypes.XREF;

@Component
public class GroupAuthorityMetaData extends DefaultEntityMetaData
{

	public static final String ENTITY_NAME = "GroupAuthority";

	public GroupAuthorityMetaData()
	{
		super(ENTITY_NAME);
		addAttribute(GroupAuthority.ID).setAuto(true).setVisible(false).setDescription("").setIdAttribute(true)
				.setNillable(false).setLabelAttribute(true);
		addAttribute(GroupAuthority.MOLGENISGROUP).setDataType(XREF).setRefEntity(new MolgenisGroupMetaData())
				.setAggregateable(true).setDescription("").setNillable(false);
		addAttribute(GroupAuthority.ROLE).setDescription("").setNillable(false);
	}
}
