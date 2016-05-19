package org.molgenis.auth;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GroupAuthorityMetaData extends SystemEntityMetaDataImpl
{
	public static final String ENTITY_NAME = "GroupAuthority";

	private MolgenisGroupMetaData molgenisGroupMetaData;

	@Override
	public void init()
	{
		setName(ENTITY_NAME);
		addAttribute(GroupAuthority.ID, ROLE_ID).setAuto(true).setVisible(false).setDescription("");
		addAttribute(GroupAuthority.MOLGENISGROUP).setDataType(XREF).setRefEntity(molgenisGroupMetaData)
				.setAggregatable(true).setDescription("").setNillable(false);
		addAttribute(GroupAuthority.ROLE).setDescription("").setNillable(false);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setMolgenisGroupMetaData(MolgenisGroupMetaData molgenisGroupMetaData)
	{
		this.molgenisGroupMetaData = requireNonNull(molgenisGroupMetaData);
	}
}
