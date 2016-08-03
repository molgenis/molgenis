package org.molgenis.data.support;

import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.meta.model.AttributeMetaData;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;

public class AttributeMetaDataUtils
{
	private static List<AttributeType> validIdAttributeTypes = newArrayList(STRING, INT, LONG);

	private AttributeMetaDataUtils()
	{
	}

	public static String getI18nAttributeName(String attrName, String languageCode)
	{
		return attrName + '-' + languageCode;
	}

	public static boolean isIdAttributeTypeAllowed(AttributeMetaData attribute)
	{
		return validIdAttributeTypes.contains(attribute.getDataType());
	}
}
