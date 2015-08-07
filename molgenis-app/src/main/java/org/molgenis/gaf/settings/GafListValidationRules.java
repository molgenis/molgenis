package org.molgenis.gaf.settings;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntity;

public class GafListValidationRules extends DefaultEntity
{
	private static final long serialVersionUID = 1L;

	public static final String ENTITY_NAME = "GafListValidationRules";
	public static final EntityMetaData META_DATA = new GafListValidationRulesMeta();

	public static final String ID = "id";
	public static final String PATTERN = "pattern";
	public static final String EXAMPLE = "example";

	public GafListValidationRules(DataService dataService)
	{
		super(META_DATA, dataService);
	}

	public String getAttribute()
	{
		return getString(ID);
	}

	public String getPattern()
	{
		return getString(PATTERN);
	}

	public String getExample()
	{
		return getString(EXAMPLE);
	}
}
