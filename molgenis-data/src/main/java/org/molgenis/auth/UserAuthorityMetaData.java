package org.molgenis.auth;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

import static org.molgenis.MolgenisFieldTypes.XREF;

@Component
public class UserAuthorityMetaData extends DefaultEntityMetaData
{

	public static final String ENTITY_NAME = "UserAuthority";
	String id;

	public UserAuthorityMetaData()
	{
		super(ENTITY_NAME);
		addAttribute(UserAuthority.ID).setAuto(true).setVisible(false).setDescription("").setIdAttribute(true)
				.setNillable(false).setLabelAttribute(true);
		addAttribute(UserAuthority.MOLGENISUSER).setDataType(XREF).setRefEntity(new MolgenisUserMetaData())
				.setAggregateable(true).setDescription("").setNillable(false);
		addAttribute(UserAuthority.ROLE).setDescription("").setNillable(false);
	}
}
