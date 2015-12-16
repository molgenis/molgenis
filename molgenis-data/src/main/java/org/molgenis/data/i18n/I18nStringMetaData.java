package org.molgenis.data.i18n;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

public class I18nStringMetaData extends DefaultEntityMetaData
{
	public static final I18nStringMetaData INSTANCE = new I18nStringMetaData();
	public static final String ENTITY_NAME = "i18nstrings";
	public static final String MSGID = "msgid";
	public static final String DESCRIPTION = "description";
	public static final String EN = "en";

	private I18nStringMetaData()
	{
		super(ENTITY_NAME);
		addAttribute(MSGID).setNillable(false).setIdAttribute(true);
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
		if (attr != null) removeAttributeMetaData(attr);
	}
}
