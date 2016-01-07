package org.molgenis.integrationtest.data;

import org.molgenis.data.support.DefaultEntityMetaData;

public class CountryMetaData extends DefaultEntityMetaData
{
	public static final CountryMetaData INSTANCE = new CountryMetaData();
	public static final String ENTITY_NAME = "Country";
	public static final String ID = "id";
	public static final String CODE = "code";
	public static final String NAME = "name";

	private CountryMetaData()
	{
		super(ENTITY_NAME, new CountriesPackage());
		this.addAttribute(ID).setIdAttribute(true).setNillable(false).setAuto(true);
		addAttribute(CODE).setValidationExpression("/^[a-z]{2}$/.test($('code').value())");
		addAttribute(NAME);
	}

}
