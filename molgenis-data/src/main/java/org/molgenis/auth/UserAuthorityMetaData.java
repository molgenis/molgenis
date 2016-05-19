package org.molgenis.auth;

import static java.util.Objects.requireNonNull;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Component
public class UserAuthorityMetaData extends SystemEntityMetaDataImpl
{
	public static final String ENTITY_NAME = "UserAuthority";

	private MolgenisUserMetaData molgenisUserMetaData;

	@Override
	public void init()
	{
		setName(ENTITY_NAME);
		addAttribute(UserAuthority.ID, ROLE_ID).setAuto(true).setVisible(false).setDescription("");
		addAttribute(UserAuthority.MOLGENISUSER).setDataType(XREF).setRefEntity(molgenisUserMetaData)
				.setAggregatable(true).setDescription("").setNillable(false);
		addAttribute(UserAuthority.ROLE).setDescription("").setNillable(false);
	}

	// setter injection instead of constructor injection to avoid unresolvable circular dependencies
	@Autowired
	public void setMolgenisUserMetaData(MolgenisUserMetaData molgenisUserMetaData)
	{
		this.molgenisUserMetaData = requireNonNull(molgenisUserMetaData);
	}
}
