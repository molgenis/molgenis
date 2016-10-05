package org.molgenis.charts;

import org.molgenis.MolgenisFieldTypes.AttributeType;

public enum MolgenisAxisType
{
	LINEAR, LOGARITHMIC, DATETIME, CATEGORY;

	MolgenisAxisType()
	{
	}

	/**
	 * Get a Molgenis axis type based on an attributeJavaType
	 *
	 * @return MolgenisAxisType
	 */
	public static MolgenisAxisType getType(AttributeType attributeFieldTypeEnum)
	{
		if (AttributeType.DECIMAL.equals(attributeFieldTypeEnum) || AttributeType.INT.equals(attributeFieldTypeEnum)
				|| AttributeType.LONG.equals(attributeFieldTypeEnum))
		{
			return MolgenisAxisType.LINEAR;
		}
		else if (AttributeType.DATE.equals(attributeFieldTypeEnum))
		{
			return MolgenisAxisType.DATETIME;
		}
		else if (AttributeType.DATE_TIME.equals(attributeFieldTypeEnum))
		{
			return MolgenisAxisType.DATETIME;
		}
		else
		{
			return MolgenisAxisType.CATEGORY;
		}
	}
}
