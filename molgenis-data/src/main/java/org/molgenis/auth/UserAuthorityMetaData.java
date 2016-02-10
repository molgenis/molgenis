package org.molgenis.auth;

import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class UserAuthorityMetaData extends DefaultEntityMetaData
{

	public static final String ENTITY_NAME = "UserAuthority";
	String id;

	public UserAuthorityMetaData()
	{
		super(ENTITY_NAME);
		addAttribute(UserAuthority.ID, ROLE_ID).setAuto(true).setVisible(false).setDescription("");
		addAttribute(UserAuthority.MOLGENISUSER).setDataType(XREF).setRefEntity(new MolgenisUserMetaData())
				.setAggregateable(true).setDescription("").setNillable(false);
		addAttribute(UserAuthority.ROLE).setDescription("").setNillable(false);
	}
}
