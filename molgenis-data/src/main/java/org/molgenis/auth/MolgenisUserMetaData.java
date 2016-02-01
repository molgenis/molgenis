package org.molgenis.auth;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.EMAIL;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_LOOKUP;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class MolgenisUserMetaData extends DefaultEntityMetaData
{
	public static final String ENTITY_NAME = "MolgenisUser";

	public MolgenisUserMetaData()
	{
		super(ENTITY_NAME);
		setDescription("Anyone who can login");

		addAttribute(MolgenisUser.ID, ROLE_ID).setAuto(true).setVisible(false)
				.setDescription("automatically generated internal id, only for internal use.");
		addAttribute(MolgenisUser.USERNAME, ROLE_LABEL, ROLE_LOOKUP).setLabel("Username").setUnique(true)
				.setDescription("").setNillable(false);
		addAttribute(MolgenisUser.PASSWORD_).setLabel("Password")
				.setDescription("This is the hashed password, enter a new plaintext password to update.")
				.setNillable(false);
		addAttribute(MolgenisUser.ACTIVATIONCODE).setLabel("Activation code").setNillable(true).setDescription(
				"Used as alternative authentication mechanism to verify user email and/or if user has lost password.");
		addAttribute(MolgenisUser.ACTIVE).setLabel("Active").setDataType(BOOL).setDefaultValue("false")
				.setDescription("Boolean to indicate if this account can be used to login").setAggregateable(true)
				.setNillable(false);
		addAttribute(MolgenisUser.SUPERUSER).setLabel("Superuser").setDataType(BOOL).setDefaultValue("false")
				.setAggregateable(true).setDescription("").setNillable(false);
		addAttribute(MolgenisUser.FIRSTNAME).setLabel("First name").setNillable(true).setDescription("");
		addAttribute(MolgenisUser.MIDDLENAMES).setLabel("Middle names").setNillable(true).setDescription("");
		addAttribute(MolgenisUser.LASTNAME, ROLE_LOOKUP).setLabel("Last name").setNillable(true).setDescription("");
		addAttribute(MolgenisUser.TITLE).setLabel("Title").setNillable(true)
				.setDescription("An academic title, e.g. Prof.dr, PhD");
		addAttribute(MolgenisUser.AFFILIATION).setLabel("Affiliation").setNillable(true).setDescription("");
		addAttribute(MolgenisUser.DEPARTMENT)
				.setDescription("Added from the old definition of MolgenisUser. Department of this contact.")
				.setNillable(true);
		addAttribute(MolgenisUser.ROLE).setDescription("Indicate role of the contact, e.g. lab worker or PI.")
				.setNillable(true);
		addAttribute(MolgenisUser.ADDRESS).setDescription("The address of the Contact.").setNillable(true)
				.setDataType(TEXT);
		addAttribute(MolgenisUser.PHONE)
				.setDescription("The telephone number of the Contact including the suitable area codes.")
				.setNillable(true);
		addAttribute(MolgenisUser.EMAIL, ROLE_LOOKUP).setDescription("The email address of the Contact.")
				.setUnique(true).setDataType(EMAIL).setNillable(false);
		addAttribute(MolgenisUser.FAX).setDescription("The fax number of the Contact.").setNillable(true);
		addAttribute(MolgenisUser.TOLLFREEPHONE).setLabel("Toll-free phone").setNillable(true)
				.setDescription("A toll free phone number for the Contact, including suitable area codes.");
		addAttribute(MolgenisUser.CITY)
				.setDescription("Added from the old definition of MolgenisUser. City of this contact.")
				.setNillable(true);
		addAttribute(MolgenisUser.COUNTRY)
				.setDescription("Added from the old definition of MolgenisUser. Country of this contact.")
				.setNillable(true);
		addAttribute(MolgenisUser.CHANGE_PASSWORD).setLabel("Change password").setDataType(BOOL)
				.setDefaultValue("false")
				.setDescription("If true the user must first change his password before he can proceed")
				.setAggregateable(true).setNillable(false);
		addAttribute(MolgenisUser.LANGUAGECODE).setLabel("Language code")
				.setDescription("Selected language for this site.").setNillable(true);
		addAttribute(MolgenisUser.GOOGLEACCOUNTID).setLabel("Google account ID")
				.setDescription("An identifier for the user, unique among all Google accounts and never reused.")
				.setNillable(true);
	}
}
