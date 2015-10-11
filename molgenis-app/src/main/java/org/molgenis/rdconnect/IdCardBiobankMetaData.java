package org.molgenis.rdconnect;

import static org.molgenis.MolgenisFieldTypes.DATETIME;
import static org.molgenis.MolgenisFieldTypes.EMAIL;
import static org.molgenis.MolgenisFieldTypes.HYPERLINK;
import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.MolgenisFieldTypes.TEXT;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class IdCardBiobankMetaData extends DefaultEntityMetaData
{
	public IdCardBiobankMetaData()
	{
		super(IdCardBiobank.ENTITY_NAME, IdCardBiobank.class);
		setBackend(IdCardRepositoryCollection.NAME);
		setLabel("Biobank or Registry");
		setDescription("Biobank or Registry data from ID-Card");

		addAttribute(IdCardBiobank.ORGANIZATION_ID).setLabel("OrganizationID").setDataType(INT).setNillable(false)
				.setIdAttribute(true);
		addAttribute(IdCardBiobank.TYPE).setLabel("type").setLookupAttribute(true).setAggregateable(true);
		addAttribute(IdCardBiobank.ALSO_LISTED_IN).setLabel("also listed in").setDataType(TEXT);
		addAttribute(IdCardBiobank.URL).setLabel("url").setDataType(TEXT);
		addAttribute(IdCardBiobank.TITLE).setLabel("title");
		addAttribute(IdCardBiobank.FIRST_NAME).setLabel("first name");
		addAttribute(IdCardBiobank.EMAIL).setLabel("email").setDataType(EMAIL);
		addAttribute(IdCardBiobank.LAST_NAME).setLabel("last name");
		addAttribute(IdCardBiobank.PHONE).setLabel("phone");
		addAttribute(IdCardBiobank.LAST_ACTIVITIES).setLabel("last activities").setDataType(DATETIME);
		addAttribute(IdCardBiobank.DATE_OF_INCLUSION).setLabel("date of inclusion").setDataType(DATETIME);
		addAttribute(IdCardBiobank.STREET2).setLabel("street2");
		addAttribute(IdCardBiobank.NAME_OF_HOST_INSTITUTION).setLabel("name of host institution");
		addAttribute(IdCardBiobank.ZIP).setLabel("zip");
		addAttribute(IdCardBiobank.STREET1).setLabel("street1");
		addAttribute(IdCardBiobank.COUNTRY).setLabel("country");
		addAttribute(IdCardBiobank.CITY).setLabel("city");
		addAttribute(IdCardBiobank.NAME).setLabel("name").setLookupAttribute(true).setLabelAttribute(true);
		addAttribute(IdCardBiobank.ID).setLabel("ID").setDataType(HYPERLINK);
		addAttribute(IdCardBiobank.TYPE_OF_HOST_INSTITUTION).setLabel("type of host institution")
				.setAggregateable(true);
		addAttribute(IdCardBiobank.TARGET_POPULATION).setLabel("target population").setAggregateable(true);
	}
}
