package org.molgenis.ui.settings;

import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.meta.RootSystemPackage.PACKAGE_SYSTEM;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class StaticContentMeta extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "StaticContent";
	public static final String STATIC_CONTENT = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String KEY = "key_";
	public static final String CONTENT = "content";

	StaticContentMeta()
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
	}

	@Override
	public void init()
	{
		addAttribute(KEY, ROLE_ID).setLabel("Key");
		addAttribute(CONTENT).setDataType(TEXT).setLabel("Content");
	}
}