package org.molgenis.gaf;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.DefaultEntityMetaData;
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
	public static class Meta extends DefaultEntityMetaData
	{
		private static final String ENTITY_NAME = "GafListSettings";
		private static final String NAME = "Name";
		private static final String VALUE = "Value";

		public Meta()
		{
			super(ENTITY_NAME);
			addAttribute(NAME).setIdAttribute(true).setNillable(false);
			addAttribute(VALUE).setNillable(false).setDataType(MolgenisFieldTypes.TEXT);
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
		Entity entity = dataService.findOne(Meta.ENTITY_NAME, name);
		return entity != null ? entity.getString(Meta.VALUE) : null;
	}
}
