package org.molgenis.data.support;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;

import java.util.EnumSet;

import static org.molgenis.data.meta.AttributeType.*;
import static org.springframework.util.StringUtils.capitalize;

public class AttributeUtils
{
	private AttributeUtils()
	{
	}

	public static String getI18nAttributeName(String attrName, String languageCode)
	{
		return attrName + capitalize(languageCode.toLowerCase());
	}

	/**
	 * Returns whether this attribute can be used as ID attribute
	 *
	 * @param attr attribute
	 * @return true if this attribute can be used as ID attribute
	 */
	public static boolean isIdAttributeTypeAllowed(Attribute attr)
	{
		return getValidIdAttributeTypes().contains(attr.getDataType());
	}

	public static EnumSet<AttributeType> getValidIdAttributeTypes()
	{
		return EnumSet.of(STRING, INT, LONG, EMAIL, HYPERLINK);
	}
}
