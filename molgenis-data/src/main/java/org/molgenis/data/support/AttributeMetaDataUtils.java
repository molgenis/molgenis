package org.molgenis.data.support;

import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.MolgenisDataException;
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
		AttributeType type = attribute.getDataType();
		if (validIdAttributeTypes.contains(type))
		{
			return true;
		}
		else
		{
			throw new MolgenisDataException("Identifier is of type [" + type
					+ "]. Id attributes can only be of type 'STRING', 'INT' or 'LONG'");
		}
	}
}
