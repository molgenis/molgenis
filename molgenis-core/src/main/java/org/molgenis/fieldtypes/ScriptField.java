package org.molgenis.fieldtypes;

import org.molgenis.MolgenisFieldTypes.AttributeType;

public class ScriptField extends TextField
{
	private static final long serialVersionUID = -3323081217879835712L;

	@Override
	public AttributeType getEnumType()
	{
		return AttributeType.SCRIPT;
	}
}
