package org.molgenis.script;

import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.meta.RootSystemPackage.PACKAGE_SYSTEM;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class MyEntityMetaData extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "MyEntity";
	public static final String SCRIPT_PARAMETER = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String NAME = "name";

	MyEntityMetaData()
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
	}

	@Override
	public void init()
	{
		addAttribute(NAME, ROLE_ID).setNillable(false).setLabel("Name label");
		addAttribute("test");
	}
}
