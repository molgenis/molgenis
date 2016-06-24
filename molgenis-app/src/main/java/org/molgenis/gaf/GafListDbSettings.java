package org.molgenis.gaf;

import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.SystemEntityMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GafListDbSettings implements GafListSettings
{
	private static final String GAF_LIST_VALIDATOR_PREFIX = "gafList.validator.";
	private static final String GAF_LIST_VALIDATOR_EXAMPLE_PREFIX = "gafList.validator.example.";
	private final DataService dataService;

	@Autowired
	public GafListDbSettings(DataService dataService)
	{
		this.dataService = dataService;
	}

	@Component
	public static class Meta extends SystemEntityMetaData
	{
		private static final String SIMPLE_NAME = "GafListSettings";
		public static final String ENTITY_NAME = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;
		private static final String NAME = "Name";
		private static final String VALUE = "Value";

		Meta()
		{
			super(SIMPLE_NAME);
		}

		@Override
		public void init()
		{
			addAttribute(NAME, ROLE_ID);
			addAttribute(VALUE).setNillable(false).setDataType(TEXT);
		}
	}

	@Override
	public String getEntityName()
	{
		return getValue("gafList.protocol.name");
	}

	@Override
	public String getExample(String columnName)
	{
		return getValue(GAF_LIST_VALIDATOR_EXAMPLE_PREFIX + columnName);
	}

	@Override
	public String getRegExpPattern(String columnName)
	{
		return getValue(GAF_LIST_VALIDATOR_PREFIX + columnName);
	}

	private String getValue(String name)
	{
		Entity entity = dataService.findOneById(Meta.ENTITY_NAME, name);
		return entity != null ? entity.getString(Meta.VALUE) : null;
	}
}
