package org.molgenis.data.examples;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.DefaultEntityMetaData;

public class UserMetaData extends DefaultEntityMetaData
{
	public static final UserMetaData INSTANCE = new UserMetaData();

	public static final String ENTITY_NAME = "User";
	public static final String USERNAME = "username";
	public static final String ACTIVE = "active";

	private UserMetaData()
	{
		super(ENTITY_NAME);
		addAttribute(USERNAME, ROLE_ID);
		addAttribute(ACTIVE).setDataType(MolgenisFieldTypes.BOOL);
	}

}
