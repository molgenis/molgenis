package org.molgenis.fieldtypes;

import org.molgenis.MolgenisFieldTypes.AttributeType;

public class CategoricalMrefField extends MrefField
{
	private static final long serialVersionUID = 1L;

	@Override
	public AttributeType getEnumType()
	{
		return AttributeType.CATEGORICAL_MREF;
	}
}
