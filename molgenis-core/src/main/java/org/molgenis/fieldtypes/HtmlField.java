package org.molgenis.fieldtypes;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;

public class HtmlField extends TextField
{
	private static final long serialVersionUID = 1L;

	@Override
	public FieldTypeEnum getEnumType()
	{
		return FieldTypeEnum.HTML;
	}
}