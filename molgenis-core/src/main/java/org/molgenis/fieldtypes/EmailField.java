package org.molgenis.fieldtypes;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;

public class EmailField extends StringField
{
	private static final long serialVersionUID = 1L;

	@Override
	public FieldTypeEnum getEnumType()
	{
		return FieldTypeEnum.EMAIL;
	}
}
