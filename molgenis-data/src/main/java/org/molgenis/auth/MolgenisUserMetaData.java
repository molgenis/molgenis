package org.molgenis.auth;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.TEXT;

@Component
public class MolgenisUserMetaData extends DefaultEntityMetaData {
    public static final String ID = "id";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password_";
    public static final String ACTIVATIONCODE= "activationCode";
    public static final String ACTIVE = "active";
    public static final String SUPERUSER = "superuser";
    public static final String FIRSTNAME = "firstName";
    public static final String MIDDLENAMES = "middleNames";
    public static final String LASTNAME = "lastName";
    public static final String TITLE = "litle";
    public static final String AFFILIATIONS = "affiliation";
    public static final String DEPARTMENT = "department";
    public static final String ROLE = "role";
    public static final String ADDRESS = "address";
    public static final String PHONE= "phone";
    public static final String EMAIL = "email";
    public static final String FAX = "fax";
    public static final String TOLLFREEPHONE = "tollFreePhone";
    public static final String CITY = "city";
    public static final String COUNTRY = "country";
    public static final String CHANGEPASSWORD = "changePassword";
    public static final String LANGUAGECODE= "languageCode";
    public static final String GOOGLEACCOUNTID = "googleAccountId";

    public MolgenisUserMetaData() {
        super("MolgenisUser");
        setDescription("Anyone who can login");
        addAttribute(ID).setAuto(true).setVisible(false)
                .setDescription("automatically generated internal id, only for internal use.").setIdAttribute(true).setNillable(false);
        addAttribute(USERNAME).setLabel("Username").setLookupAttribute(true).setUnique(true);
        addAttribute(PASSWORD).setLabel("Password").setDescription("This is the hashed password, enter a new plaintext password to update.");
        addAttribute(ACTIVATIONCODE).setLabel("Activation code").setNillable(true).setDescription("Used as alternative authentication mechanism to verify user email and/or if user has lost password.");
        addAttribute(ACTIVE).setLabel("Active").setDataType(BOOL).setDefaultValue("false").setDescription("Boolean to indicate if this account can be used to login");
        addAttribute(SUPERUSER).setLabel("Superuser").setDataType(BOOL).setDefaultValue("false");
        addAttribute(FIRSTNAME).setLabel("First name").setNillable(true);
        addAttribute(MIDDLENAMES).setLabel("Middle names").setNillable(true);
        addAttribute(LASTNAME).setLabel("Last name").setLookupAttribute(true).setNillable(true);
        addAttribute(TITLE).setLabel("Title").setNillable(true).setDescription("An academic title, e.g. Prof.dr, PhD");
        addAttribute(AFFILIATIONS).setLabel("Affiliation").setNillable(true);
        addAttribute(DEPARTMENT).setDescription("Added from the old definition of MolgenisUser. Department of this contact.").setNillable(true);
        addAttribute(ROLE).setDescription("Indicate role of the contact, e.g. lab worker or PI.").setNillable(true);
        addAttribute(ADDRESS).setDescription("The address of the Contact.").setNillable(true).setDataType(TEXT);
        addAttribute(PHONE).setDescription("The telephone number of the Contact including the suitable area codes.").setNillable(true);
        addAttribute(EMAIL).setDescription("The email address of the Contact.").setLookupAttribute(true).setUnique(true);
        addAttribute(FAX).setDescription("The fax number of the Contact.").setNillable(true);
        addAttribute(TOLLFREEPHONE).setLabel("Toll-free phone").setNillable(true).setDescription("A toll free phone number for the Contact, including suitable area codes.");
        addAttribute(CITY).setDescription("Added from the old definition of MolgenisUser. City of this contact.").setNillable(true);
        addAttribute(COUNTRY).setDescription("Added from the old definition of MolgenisUser. Country of this contact.").setNillable(true);
        addAttribute(CHANGEPASSWORD).setLabel("Change password").setDataType(BOOL).setDefaultValue("false").setDescription("If true the user must first change his password before he can proceed");
        addAttribute(LANGUAGECODE).setLabel("Language code").setDescription("Selected language for this site.").setNillable(true);
        addAttribute(GOOGLEACCOUNTID).setLabel("Google account ID").setDescription("An identifier for the user, unique among all Google accounts and never reused.").setNillable(true);
    }
}
