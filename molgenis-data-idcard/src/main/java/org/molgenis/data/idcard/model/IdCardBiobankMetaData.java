package org.molgenis.data.idcard.model;

import org.molgenis.AttributeType;
import org.molgenis.data.idcard.IdCardRepositoryCollection;
import org.molgenis.data.meta.SystemEntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;
import static org.molgenis.AttributeType.*;
import static org.molgenis.data.idcard.model.IdCardPackage.PACKAGE_ID_CARD;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.*;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;

@Component
public class IdCardBiobankMetaData extends SystemEntityType
{
	private static final String SIMPLE_NAME = "rdconnect_regbb";
	public static final String ID_CARD_BIOBANK = PACKAGE_ID_CARD + PACKAGE_SEPARATOR + SIMPLE_NAME;

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

	private final IdCardPackage idCardPackage;

	@Autowired
	IdCardBiobankMetaData(IdCardPackage idCardPackage)
	{
		super(SIMPLE_NAME, PACKAGE_ID_CARD);
		this.idCardPackage = requireNonNull(idCardPackage);
	}

	@Override
	public void init()
	{
		setPackage(idCardPackage);

		setBackend(IdCardRepositoryCollection.NAME);
		setLabel("Biobank/Registry");
		setDescription("Biobank/Registry data from ID-Card");

		addAttribute(NAME, ROLE_LABEL, ROLE_LOOKUP).setLabel("Name");
		addAttribute(TYPE, ROLE_LOOKUP).setLabel("Type").setAggregatable(true);
		addAttribute(TARGET_POPULATION).setLabel("Target population").setAggregatable(true);
		addAttribute(URL).setLabel("Website").setDataType(HYPERLINK);
		addAttribute(ID).setLabel("ID-Card").setDataType(HYPERLINK);
		addAttribute(ALSO_LISTED_IN).setLabel("also listed in").setDataType(TEXT).setVisible(false);

		addAttribute(LAST_ACTIVITIES).setLabel("Last activities").setDataType(DATE_TIME);
		addAttribute(DATE_OF_INCLUSION).setLabel("Date of inclusion").setDataType(DATE_TIME);

		addAttribute(NAME_OF_HOST_INSTITUTION).setLabel("Host institution");
		addAttribute(TYPE_OF_HOST_INSTITUTION).setLabel("Type of host institution").setAggregatable(true);
		// The salutation for the administrator in ID-Card. This is not relevant data to display in the catalogue.
		addAttribute(SALUTATION).setLabel("Salutation").setDataType(STRING).setVisible(false);
		addAttribute(FIRST_NAME).setLabel("First name").setDataType(STRING);
		addAttribute(LAST_NAME).setLabel("Last name").setDataType(STRING);
		addAttribute(EMAIL).setLabel("e-mail address").setDataType(AttributeType.EMAIL);
		addAttribute(PHONE).setLabel("Phone").setDataType(STRING);
		addAttribute(STREET1).setLabel("Address").setDataType(STRING);
		addAttribute(STREET2).setLabel("Address (cont.)").setDataType(STRING);
		addAttribute(ZIP).setLabel("Postal code").setDataType(STRING);
		addAttribute(CITY).setLabel("City");
		addAttribute(COUNTRY).setLabel("Country");

		addAttribute(ORGANIZATION_ID, ROLE_ID).setLabel("OrganizationID").setDataType(INT).setVisible(false);
	}
}
