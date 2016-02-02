package org.molgenis.auth;

import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class MolgenisGroupMemberMetaData extends DefaultEntityMetaData
{

	public static final String ENTITY_NAME = "MolgenisGroupMember";

	public MolgenisGroupMemberMetaData()
	{
		super(ENTITY_NAME);
		addAttribute(MolgenisGroupMember.ID, ROLE_ID).setAuto(true).setVisible(false).setDescription("");
		addAttribute(MolgenisGroupMember.MOLGENISUSER).setDataType(XREF).setRefEntity(new MolgenisUserMetaData())
				.setAggregateable(true).setDescription("").setNillable(false);
		addAttribute(MolgenisGroupMember.MOLGENISGROUP).setDataType(XREF).setRefEntity(new MolgenisGroupMetaData())
				.setAggregateable(true).setDescription("").setNillable(false);
	}
}
