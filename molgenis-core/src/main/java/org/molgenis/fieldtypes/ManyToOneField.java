package org.molgenis.fieldtypes;

import org.molgenis.MolgenisFieldTypes;

import static org.molgenis.MolgenisFieldTypes.AttributeType.MANY_TO_ONE;

/**
 * Field type that models the inverse relationship of ONE_TO_MANY
 */
public class ManyToOneField extends XrefField
{
	private static final long serialVersionUID = 1L;

	@Override
	public MolgenisFieldTypes.AttributeType getEnumType()
	{
		return MANY_TO_ONE;
	}
}