package org.molgenis.auth;

import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.data.meta.EntityMetaDataImpl;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.springframework.stereotype.Component;
@Component
public class UserAuthorityMetaData extends SystemEntityMetaDataImpl
{
	public static final String ENTITY_NAME = "UserAuthority";

	@Override
	public void init()
	{
		setName(ENTITY_NAME);
		addAttribute(UserAuthority.ID, ROLE_ID).setAuto(true).setVisible(false).setDescription("");
		addAttribute(UserAuthority.MOLGENISUSER).setDataType(XREF).setRefEntity(new MolgenisUserMetaData())
				.setAggregatable(true).setDescription("").setNillable(false);
		addAttribute(UserAuthority.ROLE).setDescription("").setNillable(false);
	}
}
