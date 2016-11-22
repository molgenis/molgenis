package org.molgenis.data.support;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;

import static java.lang.String.format;

public class AttributeUtils
{
	private AttributeUtils()
	{
	}

	public static String getI18nAttributeName(String attrName, String languageCode)
	{
		return attrName + '-' + languageCode;
	}

	/**
	 * Returns whether this attribute can be used as ID attribute
	 *
	 * @param attr attribute
	 * @return true if this attribute can be used as ID attribute
	 */
	public static boolean isIdAttributeTypeAllowed(Attribute attr)
	{
		AttributeType attrType = attr.getDataType();
		switch (attrType)
		{
			case BOOL:
			case CATEGORICAL:
			case CATEGORICAL_MREF:
			case COMPOUND:
			case DATE:
			case DATE_TIME:
			case DECIMAL:
			case ENUM:
			case FILE:
			case HTML:
			case MREF:
			case SCRIPT:
			case TEXT:
			case XREF:
				return false;
			case EMAIL:
			case HYPERLINK:
			case INT:
			case LONG:
			case STRING:
				return true;
			default:
				throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
		}
	}
}
