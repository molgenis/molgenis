package org.molgenis.data.i18n;

import static org.molgenis.data.meta.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.meta.RootSystemPackage.PACKAGE_SYSTEM;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.meta.AttributeMetaData;
import org.molgenis.data.meta.SystemEntityMetaDataImpl;
import org.springframework.stereotype.Component;

@Component
public class I18nStringMetaData extends SystemEntityMetaDataImpl
{
	private static final String SIMPLE_NAME = "i18nstrings";
	public static final String I18N_STRING = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

	public static final String MSGID = "msgid";
	public static final String DESCRIPTION = "description";
	public static final String EN = "en";

	I18nStringMetaData()
	{
		super(SIMPLE_NAME, PACKAGE_SYSTEM);
	}

	@Override
	public void init()
	{
		addAttribute(MSGID, ROLE_ID);
		addAttribute(DESCRIPTION).setNillable(true).setDataType(MolgenisFieldTypes.TEXT);

		addAttribute(EN).setNillable(true).setDataType(MolgenisFieldTypes.TEXT);
	}

	public boolean addLanguage(String languageCode)
	{
		if (getAttribute(languageCode) == null)
		{
			addAttribute(languageCode).setNillable(true).setDataType(MolgenisFieldTypes.TEXT);
			return true;
		}

		return false;
	}

	public void removeLanguage(String languageCode)
	{
		AttributeMetaData attr = getAttribute(languageCode);
		if (attr != null) removeAttribute(attr);
	}
}
