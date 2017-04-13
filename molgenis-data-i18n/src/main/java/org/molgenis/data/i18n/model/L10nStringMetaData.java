package org.molgenis.data.i18n.model;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

import static org.molgenis.data.meta.AttributeType.TEXT;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class L10nStringMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "L10nString";
	public static final String L10N_STRING = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String ID = "id";
	public static final String MSGID = "msgid";
	public static final String NAMESPACE = "namespace";
	public static final String DESCRIPTION = "description";

	L10nStringMetaData()
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
	}

	@Override
	public void init()
	{
		setLabel("Localization");
		setDescription("Translated language strings");
		addAttribute(ID, ROLE_ID).setAuto(true).setNillable(false);
		addAttribute(MSGID).setNillable(false);
		addAttribute(NAMESPACE).setNillable(false);
		addAttribute(DESCRIPTION).setNillable(true).setDataType(TEXT);
	}
}
