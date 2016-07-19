package org.molgenis.fieldtypes;

import org.molgenis.MolgenisFieldTypes.AttributeType;

public class CategoricalField extends XrefField
{
	private static final long serialVersionUID = 1L;

	@Override
	public AttributeType getEnumType()
	{
		return AttributeType.CATEGORICAL;
	}
}
