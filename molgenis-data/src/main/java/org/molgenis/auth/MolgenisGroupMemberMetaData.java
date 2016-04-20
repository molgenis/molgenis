package org.molgenis.auth;

import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.data.meta.EntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class MolgenisGroupMemberMetaData extends EntityMetaData
{

	public static final String ENTITY_NAME = "MolgenisGroupMember";

	public MolgenisGroupMemberMetaData()
	{
		super(ENTITY_NAME);
		addAttribute(MolgenisGroupMember.ID, ROLE_ID).setAuto(true).setVisible(false).setDescription("");
		addAttribute(MolgenisGroupMember.MOLGENISUSER).setDataType(XREF).setRefEntity(new MolgenisUserMetaData())
				.setAggregatable(true).setDescription("").setNillable(false);
		addAttribute(MolgenisGroupMember.MOLGENISGROUP).setDataType(XREF).setRefEntity(new MolgenisGroupMetaData())
				.setAggregatable(true).setDescription("").setNillable(false);
	}
}
