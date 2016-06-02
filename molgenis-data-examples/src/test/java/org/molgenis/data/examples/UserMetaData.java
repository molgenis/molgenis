package org.molgenis.data.examples;

import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;

public class UserMetaData extends SystemEntityMetaDataImpl
{
	public static final String USER = "User";

	public static final String USERNAME = "username";
	public static final String ACTIVE = "active";

	UserMetaData()
	{
		super(USER);
	}

	@Override
	public void init()
	{
		addAttribute(USERNAME, ROLE_ID);
		addAttribute(ACTIVE).setDataType(MolgenisFieldTypes.BOOL);
	}
}
