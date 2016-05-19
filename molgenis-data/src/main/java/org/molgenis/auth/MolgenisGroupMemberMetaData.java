package org.molgenis.auth;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MolgenisGroupMemberMetaData extends SystemEntityMetaDataImpl
{
	public static final String ENTITY_NAME = "MolgenisGroupMember";

	private MolgenisUserMetaData molgenisUserMetaData;
	private MolgenisGroupMetaData MolgenisGroupMetaData;

	@Override
	public void init()
	{
		setName(ENTITY_NAME);
		addAttribute(MolgenisGroupMember.ID, ROLE_ID).setAuto(true).setVisible(false).setDescription("");
		addAttribute(MolgenisGroupMember.MOLGENISUSER).setDataType(XREF).setRefEntity(molgenisUserMetaData)
				.setAggregatable(true).setDescription("").setNillable(false);
		addAttribute(MolgenisGroupMember.MOLGENISGROUP).setDataType(XREF).setRefEntity(MolgenisGroupMetaData)
				.setAggregatable(true).setDescription("").setNillable(false);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setMolgenisUserMetaData(MolgenisUserMetaData molgenisUserMetaData)
	{
		this.molgenisUserMetaData = requireNonNull(molgenisUserMetaData);
	}

	@Autowired
	public void setMolgenisGroupMetaData(MolgenisGroupMetaData molgenisGroupMetaData)
	{
		this.MolgenisGroupMetaData = requireNonNull(molgenisGroupMetaData);
	}
}
