package org.molgenis.data.i18n.model;

import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.stereotype.Component;

import static org.molgenis.MolgenisFieldTypes.AttributeType.TEXT;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

@Component
public class I18nStringMetaData extends SystemEntityMetaData
{
	private static final String SIMPLE_NAME = "i18nstrings";
	public static final String I18N_STRING = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String MSGID = "msgid";
	public static final String DESCRIPTION = "description";

	I18nStringMetaData()
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
	}

	@Override
	public void init()
	{
		addAttribute(MSGID, ROLE_ID);
		addAttribute(DESCRIPTION).setNillable(true).setDataType(TEXT);
	}
}
