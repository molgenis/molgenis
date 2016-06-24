package org.molgenis.fieldtypes;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;

public class FileField extends XrefField
{
	private static final long serialVersionUID = 1L;

	@Override
	public FieldTypeEnum getEnumType()
	{
		return FieldTypeEnum.FILE;
	}
}