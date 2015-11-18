package org.molgenis.data.idcard.model;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntity;

public class IdCardBiobank extends DefaultEntity
{
	private static final long serialVersionUID = 1L;

	public static final String ENTITY_NAME = "rdconnect_regbb";
	public static final EntityMetaData META_DATA = new IdCardBiobankMetaData();

	public static final String ORGANIZATION_ID = "OrganizationID";
	public static final String TYPE = "type";
	public static final String ALSO_LISTED_IN = "also_listed_in";
	public static final String URL = "url";
	public static final String SALUTATION = "title";
	public static final String FIRST_NAME = "first_name";
	public static final String EMAIL = "email";
	public static final String LAST_NAME = "last_name";
	public static final String PHONE = "phone";
	public static final String LAST_ACTIVITIES = "last_activities";
	public static final String DATE_OF_INCLUSION = "date_of_inclusion";
	public static final String STREET2 = "street2";
	public static final String NAME_OF_HOST_INSTITUTION = "name_of_host_institution";
	public static final String ZIP = "zip";
	public static final String STREET1 = "street1";
	public static final String COUNTRY = "country";
	public static final String CITY = "city";
	public static final String NAME = "name";
	public static final String ID = "ID";
	public static final String TYPE_OF_HOST_INSTITUTION = "type_of_host_institution";
	public static final String TARGET_POPULATION = "target_population";

	public IdCardBiobank(DataService dataService)
	{
		super(META_DATA, dataService);
	}
}
