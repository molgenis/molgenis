package org.molgenis.auth;

import static org.molgenis.auth.MolgenisUserMetaData.ACTIVATIONCODE;
import static org.molgenis.auth.MolgenisUserMetaData.ACTIVE;
import static org.molgenis.auth.MolgenisUserMetaData.ADDRESS;
import static org.molgenis.auth.MolgenisUserMetaData.AFFILIATION;
import static org.molgenis.auth.MolgenisUserMetaData.CHANGE_PASSWORD;
import static org.molgenis.auth.MolgenisUserMetaData.CITY;
import static org.molgenis.auth.MolgenisUserMetaData.COUNTRY;
import static org.molgenis.auth.MolgenisUserMetaData.DEPARTMENT;
import static org.molgenis.auth.MolgenisUserMetaData.EMAIL;
import static org.molgenis.auth.MolgenisUserMetaData.FAX;
import static org.molgenis.auth.MolgenisUserMetaData.FIRSTNAME;
import static org.molgenis.auth.MolgenisUserMetaData.GOOGLEACCOUNTID;
import static org.molgenis.auth.MolgenisUserMetaData.ID;
import static org.molgenis.auth.MolgenisUserMetaData.LANGUAGECODE;
import static org.molgenis.auth.MolgenisUserMetaData.LASTNAME;
import static org.molgenis.auth.MolgenisUserMetaData.MIDDLENAMES;
import static org.molgenis.auth.MolgenisUserMetaData.PASSWORD_;
import static org.molgenis.auth.MolgenisUserMetaData.PHONE;
import static org.molgenis.auth.MolgenisUserMetaData.ROLE;
import static org.molgenis.auth.MolgenisUserMetaData.SUPERUSER;
import static org.molgenis.auth.MolgenisUserMetaData.TITLE;
import static org.molgenis.auth.MolgenisUserMetaData.TOLLFREEPHONE;
import static org.molgenis.auth.MolgenisUserMetaData.USERNAME;

import org.molgenis.data.Entity;
import org.molgenis.data.support.StaticEntity;

public class MolgenisUser extends StaticEntity
{
	public MolgenisUser(Entity entity)
	{
		super(entity);
	}

	public MolgenisUser(MolgenisUserMetaData molgenisUserMetaData)
	{
		super(molgenisUserMetaData);
	}

	public MolgenisUser(String id, MolgenisUserMetaData molgenisUserMetaData)
	{
		super(molgenisUserMetaData);
		setId(id);
	}

	public String getId()
	{
		return getString(ID);
	}

	public void setId(String id)
	{
		set(ID, id);
	}

	public String getUsername()
	{
		return getString(USERNAME);
	}

	public void setUsername(String username)
	{
		set(USERNAME, username);
	}

	public String getPassword()
	{
		return getString(PASSWORD_);
	}

	public void setPassword(String password)
	{
		set(PASSWORD_, password);
	}

	public String getActivationCode()
	{
		return getString(ACTIVATIONCODE);
	}

	public void setActivationCode(String activationCode)
	{
		set(ACTIVATIONCODE, activationCode);
	}

	public Boolean isActive()
	{
		return getBoolean(ACTIVE);
	}

	public void setActive(Boolean active)
	{
		set(ACTIVE, active);
	}

	public Boolean isSuperuser()
	{
		return getBoolean(SUPERUSER);
	}

	public void setSuperuser(Boolean superuser)
	{
		set(SUPERUSER, superuser);
	}

	public String getFirstName()
	{
		return getString(FIRSTNAME);
	}

	public void setFirstName(String firstName)
	{
		set(FIRSTNAME, firstName);
	}

	public String getMiddleNames()
	{
		return getString(MIDDLENAMES);
	}

	public void setMiddleNames(String middleNames)
	{
		set(MIDDLENAMES, middleNames);
	}

	public String getLastName()
	{
		return getString(LASTNAME);
	}

	public void setLastName(String lastName)
	{
		set(LASTNAME, lastName);
	}

	public String getTitle()
	{
		return getString(TITLE);
	}

	public void setTitle(String title)
	{
		set(TITLE, title);
	}

	public String getAffiliation()
	{
		return getString(AFFILIATION);
	}

	public void setAffiliation(String affiliation)
	{
		set(AFFILIATION, affiliation);
	}

	public String getDepartment()
	{
		return getString(DEPARTMENT);
	}

	public void setDepartment(String department)
	{
		set(DEPARTMENT, department);
	}

	public String getRole()
	{
		return getString(ROLE);
	}

	public void setRole(String role)
	{
		set(ROLE, role);
	}

	public String getAddress()
	{
		return getString(ADDRESS);
	}

	public void setAddress(String address)
	{
		set(ADDRESS, address);
	}

	public String getPhone()
	{
		return getString(PHONE);
	}

	public void setPhone(String phone)
	{
		set(PHONE, phone);
	}

	public String getEmail()
	{
		return getString(EMAIL);
	}

	public void setEmail(String email)
	{
		set(EMAIL, email);
	}

	public String getFax()
	{
		return getString(FAX);
	}

	public void setFax(String fax)
	{
		set(FAX, fax);
	}

	public String getTollFreePhone()
	{
		return getString(TOLLFREEPHONE);
	}

	public void setTollFreePhone(String tollFreePhone)
	{
		set(TOLLFREEPHONE, tollFreePhone);
	}

	public String getCity()
	{
		return getString(CITY);
	}

	public void setCity(String city)
	{
		set(CITY, city);
	}

	public String getCountry()
	{
		return getString(COUNTRY);
	}

	public void setCountry(String country)
	{
		set(COUNTRY, country);
	}

	public Boolean isChangePassword()
	{
		return getBoolean(CHANGE_PASSWORD);
	}

	public void setChangePassword(Boolean changePassword)
	{
		set(CHANGE_PASSWORD, changePassword);
	}

	public String getLanguageCode()
	{
		return getString(LANGUAGECODE);
	}

	public void setLanguageCode(String languageCode)
	{
		set(LANGUAGECODE, languageCode);
	}

	public String getGoogleAccountId()
	{
		return getString(GOOGLEACCOUNTID);
	}

	public void setGoogleAccountId(String googleAccountId)
	{
		set(GOOGLEACCOUNTID, googleAccountId);
	}
}
