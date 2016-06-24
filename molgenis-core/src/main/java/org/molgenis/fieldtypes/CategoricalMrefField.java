package org.molgenis.fieldtypes;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;

public class CategoricalMrefField extends MrefField
{
	private static final long serialVersionUID = 1L;

	@Override
	public FieldTypeEnum getEnumType()
	{
		return FieldTypeEnum.CATEGORICAL_MREF;
	}
}
