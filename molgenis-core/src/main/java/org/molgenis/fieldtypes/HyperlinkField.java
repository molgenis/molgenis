package org.molgenis.fieldtypes;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;

public class HyperlinkField extends StringField
{
	private static final long serialVersionUID = 1L;

	@Override
	public FieldTypeEnum getEnumType()
	{
		return FieldTypeEnum.HYPERLINK;
	}
}
