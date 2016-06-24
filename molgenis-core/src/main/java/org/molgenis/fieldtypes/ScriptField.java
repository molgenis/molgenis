package org.molgenis.fieldtypes;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;

public class ScriptField extends TextField
{
	private static final long serialVersionUID = -3323081217879835712L;

	@Override
	public FieldTypeEnum getEnumType()
	{
		return FieldTypeEnum.SCRIPT;
	}
}
