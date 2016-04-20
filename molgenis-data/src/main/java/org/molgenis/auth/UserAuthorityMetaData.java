package org.molgenis.auth;

import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.data.meta.EntityMetaData;
import org.springframework.stereotype.Component;
@Component
public class UserAuthorityMetaData extends EntityMetaData
{

	public static final String ENTITY_NAME = "UserAuthority";
	String id;

	public UserAuthorityMetaData()
	{
		super(ENTITY_NAME);
		addAttribute(UserAuthority.ID, ROLE_ID).setAuto(true).setVisible(false).setDescription("");
		addAttribute(UserAuthority.MOLGENISUSER).setDataType(XREF).setRefEntity(new MolgenisUserMetaData())
				.setAggregatable(true).setDescription("").setNillable(false);
		addAttribute(UserAuthority.ROLE).setDescription("").setNillable(false);
	}
}
