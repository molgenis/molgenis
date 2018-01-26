package org.molgenis.core.ui.settings;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class StaticContentMeta extends SystemEntityType
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
		setLabel("Static content");
		addAttribute(KEY, ROLE_ID).setLabel("Key");
		addAttribute(CONTENT).setDataType(TEXT).setLabel("Content");
	}
}