package org.molgenis.data.idcard.model;

import static org.molgenis.MolgenisFieldTypes.DATETIME;
import static org.molgenis.MolgenisFieldTypes.EMAIL;
import static org.molgenis.MolgenisFieldTypes.HYPERLINK;
import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LOOKUP;

import org.molgenis.data.idcard.IdCardRepositoryCollection;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class IdCardBiobankMetaData extends DefaultEntityMetaData
{
	public IdCardBiobankMetaData()
	{
		super(IdCardBiobank.ENTITY_NAME, IdCardBiobank.class);
		setBackend(IdCardRepositoryCollection.NAME);
		setLabel("Biobank/Registry");
		setDescription("Biobank/Registry data from ID-Card");

		addAttribute(IdCardBiobank.NAME, ROLE_LABEL, ROLE_LOOKUP).setLabel("Name");
		addAttribute(IdCardBiobank.TYPE, ROLE_LOOKUP).setLabel("Type").setAggregateable(true);
		addAttribute(IdCardBiobank.TARGET_POPULATION).setLabel("Target population").setAggregateable(true);
		addAttribute(IdCardBiobank.URL).setLabel("Website").setDataType(HYPERLINK);
		addAttribute(IdCardBiobank.ID).setLabel("ID-Card").setDataType(HYPERLINK);
		addAttribute(IdCardBiobank.ALSO_LISTED_IN).setLabel("also listed in").setDataType(TEXT).setVisible(false);

		addAttribute(IdCardBiobank.LAST_ACTIVITIES).setLabel("Last activities").setDataType(DATETIME);
		addAttribute(IdCardBiobank.DATE_OF_INCLUSION).setLabel("Date of inclusion").setDataType(DATETIME);

		addAttribute(IdCardBiobank.NAME_OF_HOST_INSTITUTION).setLabel("Host institution");
		addAttribute(IdCardBiobank.TYPE_OF_HOST_INSTITUTION).setLabel("Type of host institution")
				.setAggregateable(true);
		// The salutation for the administrator in ID-Card. This is not relevant data to display in the catalogue.
		addAttribute(IdCardBiobank.SALUTATION).setLabel("Salutation").setDataType(STRING).setVisible(false);
		addAttribute(IdCardBiobank.FIRST_NAME).setLabel("First name").setDataType(STRING);
		addAttribute(IdCardBiobank.LAST_NAME).setLabel("Last name").setDataType(STRING);
		addAttribute(IdCardBiobank.EMAIL).setLabel("e-mail address").setDataType(EMAIL);
		addAttribute(IdCardBiobank.PHONE).setLabel("Phone").setDataType(STRING);
		addAttribute(IdCardBiobank.STREET1).setLabel("Address").setDataType(STRING);
		addAttribute(IdCardBiobank.STREET2).setLabel("Address (cont.)").setDataType(STRING);
		addAttribute(IdCardBiobank.ZIP).setLabel("Postal code").setDataType(STRING);
		addAttribute(IdCardBiobank.CITY).setLabel("City");
		addAttribute(IdCardBiobank.COUNTRY).setLabel("Country");

		addAttribute(IdCardBiobank.ORGANIZATION_ID, ROLE_ID).setLabel("OrganizationID").setDataType(INT)
				.setVisible(false);
	}
}
