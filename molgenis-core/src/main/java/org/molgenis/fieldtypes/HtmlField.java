package org.molgenis.fieldtypes;

import org.molgenis.MolgenisFieldTypes.AttributeType;

public class HtmlField extends TextField
{
	private static final long serialVersionUID = 1L;

	@Override
	public AttributeType getEnumType()
	{
		return AttributeType.HTML;
	}
}