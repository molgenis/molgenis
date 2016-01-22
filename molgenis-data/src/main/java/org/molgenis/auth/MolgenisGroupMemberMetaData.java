package org.molgenis.auth;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

import static org.molgenis.MolgenisFieldTypes.XREF;

@Component
public class MolgenisGroupMemberMetaData extends DefaultEntityMetaData
{

	public static final String ENTITY_NAME = "MolgenisGroupMember";

	public MolgenisGroupMemberMetaData()
	{
		super(ENTITY_NAME);
		addAttribute(MolgenisGroupMember.ID).setAuto(true).setVisible(false).setDescription("").setIdAttribute(true)
				.setNillable(false).setLabelAttribute(true);
		addAttribute(MolgenisGroupMember.MOLGENISUSER).setDataType(XREF).setRefEntity(new MolgenisUserMetaData())
				.setAggregateable(true).setDescription("").setNillable(false);
		addAttribute(MolgenisGroupMember.MOLGENISGROUP).setDataType(XREF).setRefEntity(new MolgenisGroupMetaData())
				.setAggregateable(true).setDescription("").setNillable(false);
	}
}
