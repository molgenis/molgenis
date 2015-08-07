package org.molgenis.gaf.settings;

import org.molgenis.data.support.DefaultEntityMetaData;

public class GafListValidationRulesMeta extends DefaultEntityMetaData
{
	public GafListValidationRulesMeta()
	{
		super(GafListValidationRules.ENTITY_NAME, GafListValidationRules.class);

		addAttribute(GafListValidationRules.ID).setIdAttribute(true).setNillable(false).setLabel("Id")
				.setDescription("Validation rule id: <entity name>.<attribute name>");
		addAttribute(GafListValidationRules.PATTERN).setNillable(false).setLabel("Pattern");
		addAttribute(GafListValidationRules.EXAMPLE).setNillable(false).setLabel("Example");
	}
}
