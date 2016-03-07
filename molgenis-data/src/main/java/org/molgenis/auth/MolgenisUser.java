package org.molgenis.auth;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;

import java.util.LinkedHashSet;
import java.util.Set;

public class MolgenisUser extends org.molgenis.data.support.AbstractEntity implements org.molgenis.data.Entity
{
	private static final long serialVersionUID = 5975610105951567982L;

	public static final String ENTITY_NAME = "molgenisUser";
	public static final String USERNAME = "username";
	public static final String SUPERUSER = "superuser";
	public static final String PASSWORD_ = "password_";
	public static final String EMAIL = "Email";
	public static final String GOOGLEACCOUNTID = "googleAccountId";
	public static final String ACTIVATIONCODE = "activationCode";
	public static final String ACTIVE = "active";
	public static final String CHANGE_PASSWORD = "changePassword";
	public static final String FIRSTNAME = "FirstName";
	public static final String MIDDLENAMES = "MiddleNames";
	public static final String LASTNAME = "LastName";
	public static final String TITLE = "Title";
	public static final String AFFILIATION = "Affiliation";
	public static final String DEPARTMENT = "Department";
	public static final String ROLE = "Role";
	public static final String ADDRESS = "Address";
	public static final String PHONE = "Phone";
	public static final String TOLLFREEPHONE = "tollFreePhone";
	public static final String FAX = "Fax";
	public static final String CITY = "City";
	public static final String COUNTRY = "Country";
	public static final String LANGUAGECODE = "languageCode";
	public static final String ID = "id";
	public static final String ADDESS = "Addess";

	String id;
	String username;
	String password;
	String activationCode;
	Boolean active;
	Boolean superuser;
	String firstName;
	String middleNames;
	String lastName;
	String title;
	String affiliation;
	String department;
	String role;
	String address;
	String phone;
	String email;
	String fax;
	String tollFreePhone;
	String city;
	String country;
	Boolean changePassword;
	String languageCode;
	String googleAccountId;

	public static String getEntityName()
	{
		return ENTITY_NAME;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getActivationCode()
	{
		return activationCode;
	}

	public void setActivationCode(String activationCode)
	{
		this.activationCode = activationCode;
	}

	public Boolean isActive()
	{
		return active;
	}

	public void setActive(Boolean active)
	{
		this.active = active;
	}

	public Boolean isSuperuser()
	{
		return superuser;
	}

	public void setSuperuser(Boolean superuser)
	{
		this.superuser = superuser;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public String getMiddleNames()
	{
		return middleNames;
	}

	public void setMiddleNames(String middleNames)
	{
		this.middleNames = middleNames;
	}

	public String getLastName()
	{
		return lastName;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getAffiliation()
	{
		return affiliation;
	}

	public void setAffiliation(String affiliation)
	{
		this.affiliation = affiliation;
	}

	public String getDepartment()
	{
		return department;
	}

	public void setDepartment(String department)
	{
		this.department = department;
	}

	public String getRole()
	{
		return role;
	}

	public void setRole(String role)
	{
		this.role = role;
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress(String address)
	{
		this.address = address;
	}

	public String getPhone()
	{
		return phone;
	}

	public void setPhone(String phone)
	{
		this.phone = phone;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getFax()
	{
		return fax;
	}

	public void setFax(String fax)
	{
		this.fax = fax;
	}

	public String getTollFreePhone()
	{
		return tollFreePhone;
	}

	public void setTollFreePhone(String tollFreePhone)
	{
		this.tollFreePhone = tollFreePhone;
	}

	public String getCity()
	{
		return city;
	}

	public void setCity(String city)
	{
		this.city = city;
	}

	public String getCountry()
	{
		return country;
	}

	public void setCountry(String country)
	{
		this.country = country;
	}

	public Boolean isChangePassword()
	{
		return changePassword;
	}

	public void setChangePassword(Boolean changePassword)
	{
		this.changePassword = changePassword;
	}

	public String getLanguageCode()
	{
		return languageCode;
	}

	public void setLanguageCode(String languageCode)
	{
		this.languageCode = languageCode;
	}

	public String getGoogleAccountId()
	{
		return googleAccountId;
	}

	public void setGoogleAccountId(String googleAccountId)
	{
		this.googleAccountId = googleAccountId;
	}

	@Override
	public Object get(String name)
	{
		if (name.equals(ID)) return getId();
		if (name.equals(USERNAME)) return getUsername();
		if (name.equals(PASSWORD_)) return getPassword();
		if (name.equals(ACTIVATIONCODE)) return getActivationCode();
		if (name.equals(ACTIVE)) return isActive();
		if (name.equals(SUPERUSER)) return isSuperuser();
		if (name.equals(FIRSTNAME)) return getFirstName();
		if (name.equals(MIDDLENAMES)) return getMiddleNames();
		if (name.equals(LASTNAME)) return getLastName();
		if (name.equals(TITLE)) return getTitle();
		if (name.equals(AFFILIATION)) return getAffiliation();
		if (name.equals(DEPARTMENT)) return getDepartment();
		if (name.equals(ROLE)) return getRole();
		if (name.equals(ADDRESS)) return getAddress();
		if (name.equals(PHONE)) return getPhone();
		if (name.equals(EMAIL)) return getEmail();
		if (name.equals(FAX)) return getFax();
		if (name.equals(TOLLFREEPHONE)) return getTollFreePhone();
		if (name.equals(CITY)) return getCity();
		if (name.equals(COUNTRY)) return getCountry();
		if (name.equals(CHANGE_PASSWORD)) return isChangePassword();
		if (name.equals(LANGUAGECODE)) return getLanguageCode();
		if (name.equals(GOOGLEACCOUNTID)) return getGoogleAccountId();
		return null;
	}

	@Override
	public void set(Entity entity)
	{
		set(entity, true);
	}

	public void set(org.molgenis.data.Entity entity, boolean strict)
	{
		if (entity.getString(ID) != null) this.setId(entity.getString(ID));
		if (entity.getString("MolgenisUser_id") != null) this.setId(entity.getString("MolgenisUser_id"));
		if (entity.getString(USERNAME) != null) this.setUsername(entity.getString(USERNAME));
		if (entity.getString("MolgenisUser_username") != null)
			this.setUsername(entity.getString("MolgenisUser_username"));
		if (entity.getString(PASSWORD_) != null) this.setPassword(entity.getString(PASSWORD_));
		if (entity.getString("MolgenisUser_password_") != null)
			this.setPassword(entity.getString("MolgenisUser_password_"));
		if (entity.getString(ACTIVATIONCODE) != null) this.setActivationCode(entity.getString("activationCode"));
		if (entity.getString("MolgenisUser_activationCode") != null)
			this.setActivationCode(entity.getString("MolgenisUser_activationCode"));
		if (entity.getBoolean(ACTIVE) != null) this.setActive(entity.getBoolean(ACTIVE));
		if (entity.getBoolean("MolgenisUser_active") != null) this.setActive(entity.getBoolean("MolgenisUser_active"));
		if (entity.getBoolean(SUPERUSER) != null) this.setSuperuser(entity.getBoolean(SUPERUSER));
		if (entity.getBoolean("MolgenisUser_superuser") != null)
			this.setSuperuser(entity.getBoolean("MolgenisUser_superuser"));
		if (entity.getString(FIRSTNAME) != null) this.setFirstName(entity.getString(FIRSTNAME));
		if (entity.getString("MolgenisUser_FirstName") != null)
			this.setFirstName(entity.getString("MolgenisUser_FirstName"));
		if (entity.getString(MIDDLENAMES) != null) this.setMiddleNames(entity.getString(MIDDLENAMES));
		if (entity.getString("MolgenisUser_MiddleNames") != null)
			this.setMiddleNames(entity.getString("MolgenisUser_MiddleNames"));
		if (entity.getString(LASTNAME) != null) this.setLastName(entity.getString(LASTNAME));
		if (entity.getString("MolgenisUser_LastName") != null)
			this.setLastName(entity.getString("MolgenisUser_LastName"));
		if (entity.getString(TITLE) != null) this.setTitle(entity.getString(TITLE));
		if (entity.getString("MolgenisUser_Title") != null) this.setTitle(entity.getString("MolgenisUser_Title"));
		if (entity.getString(AFFILIATION) != null) this.setAffiliation(entity.getString(AFFILIATION));
		if (entity.getString("MolgenisUser_Affiliation") != null)
			this.setAffiliation(entity.getString("MolgenisUser_Affiliation"));
		if (entity.getString(DEPARTMENT) != null) this.setDepartment(entity.getString(DEPARTMENT));
		if (entity.getString("MolgenisUser_Department") != null)
			this.setDepartment(entity.getString("MolgenisUser_Department"));
		if (entity.getString(ROLE) != null) this.setRole(entity.getString("Role"));
		if (entity.getString("MolgenisUser_Role") != null) this.setRole(entity.getString("MolgenisUser_Role"));
		if (entity.getString(ADDESS) != null) this.setAddress(entity.getString(ADDRESS));
		if (entity.getString("MolgenisUser_Address") != null) this.setAddress(entity.getString("MolgenisUser_Address"));
		if (entity.getString(PHONE) != null) this.setPhone(entity.getString(PHONE));
		if (entity.getString("MolgenisUser_Phone") != null) this.setPhone(entity.getString("MolgenisUser_Phone"));
		if (entity.getString(EMAIL) != null) this.setEmail(entity.getString(EMAIL));
		if (entity.getString("MolgenisUser_Email") != null) this.setEmail(entity.getString("MolgenisUser_Email"));
		if (entity.getString(FAX) != null) this.setFax(entity.getString(FAX));
		if (entity.getString("MolgenisUser_Fax") != null) this.setFax(entity.getString("MolgenisUser_Fax"));
		if (entity.getString(TOLLFREEPHONE) != null) this.setTollFreePhone(entity.getString(TOLLFREEPHONE));
		if (entity.getString("MolgenisUser_tollFreePhone") != null)
			this.setTollFreePhone(entity.getString("MolgenisUser_tollFreePhone"));
		if (entity.getString(CITY) != null) this.setCity(entity.getString(CITY));
		if (entity.getString("MolgenisUser_City") != null) this.setCity(entity.getString("MolgenisUser_City"));
		if (entity.getString(COUNTRY) != null) this.setCountry(entity.getString(COUNTRY));
		if (entity.getString("MolgenisUser_Country") != null) this.setCountry(entity.getString("MolgenisUser_Country"));
		if (entity.getBoolean(CHANGE_PASSWORD) != null) this.setChangePassword(entity.getBoolean(CHANGE_PASSWORD));
		if (entity.getBoolean("MolgenisUser_changePassword") != null)
			this.setChangePassword(entity.getBoolean("MolgenisUser_changePassword"));
		if (entity.getString(LANGUAGECODE) != null) this.setLanguageCode(entity.getString(LANGUAGECODE));
		if (entity.getString("MolgenisUser_languageCode") != null)
			this.setLanguageCode(entity.getString("MolgenisUser_languageCode"));
		if (entity.getString(GOOGLEACCOUNTID) != null) this.setGoogleAccountId(entity.getString(GOOGLEACCOUNTID));
		if (entity.getString("MolgenisUser_googleAccountId") != null)
			this.setGoogleAccountId(entity.getString("MolgenisUser_googleAccountId"));
	}

	@Override
	public String toString()
	{
		return this.toString(false);
	}

	public String toString(boolean verbose)
	{
		StringBuilder sb = new StringBuilder("MolgenisUser(");
		sb.append("id='" + getId() + "' ");
		sb.append("username='" + getUsername() + "' ");
		sb.append("password_='" + getPassword() + "' ");
		sb.append("activationCode='" + getActivationCode() + "' ");
		sb.append("active='" + isActive() + "' ");
		sb.append("superuser='" + isSuperuser() + "' ");
		sb.append("firstName='" + getFirstName() + "' ");
		sb.append("middleNames='" + getMiddleNames() + "' ");
		sb.append("lastName='" + getLastName() + "' ");
		sb.append("title='" + getTitle() + "' ");
		sb.append("affiliation='" + getAffiliation() + "' ");
		sb.append("department='" + getDepartment() + "' ");
		sb.append("role='" + getRole() + "' ");
		sb.append("address='" + getAddress() + "' ");
		sb.append("phone='" + getPhone() + "' ");
		sb.append("email='" + getEmail() + "' ");
		sb.append("fax='" + getFax() + "' ");
		sb.append("tollFreePhone='" + getTollFreePhone() + "' ");
		sb.append("city='" + getCity() + "' ");
		sb.append("country='" + getCountry() + "' ");
		sb.append("changePassword='" + isChangePassword() + "' ");
		sb.append("languageCode='" + getLanguageCode() + "' ");
		sb.append("googleAccountId='" + getGoogleAccountId() + "'");
		sb.append(");");
		return sb.toString();
	}

	@Override
	public String getIdValue()
	{
		return getId();
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		Set<String> attributeNames = new LinkedHashSet<String>();
		for (AttributeMetaData attr : new MolgenisUserMetaData().getAttributes())
		{
			attributeNames.add(attr.getName());
		}

		return attributeNames;
	}

	@Override
	public void set(String attributeName, Object value)
	{
		if (ID.equals(attributeName))
		{
			this.setId((String) value);
			return;
		}
		if (USERNAME.equals(attributeName))
		{
			this.setUsername((String) value);
			return;
		}
		if (PASSWORD_.equals(attributeName))
		{
			this.setPassword((String) value);
			return;
		}
		if (ACTIVATIONCODE.equals(attributeName))
		{
			this.setActivationCode((String) value);
			return;
		}
		if (ACTIVE.equals(attributeName))
		{
			this.setActive((Boolean) value);
			return;
		}
		if (SUPERUSER.equals(attributeName))
		{
			this.setSuperuser((Boolean) value);
			return;
		}
		if (FIRSTNAME.equals(attributeName))
		{
			this.setFirstName((String) value);
			return;
		}
		if (MIDDLENAMES.equals(attributeName))
		{
			this.setMiddleNames((String) value);
			return;
		}
		if (LASTNAME.equals(attributeName))
		{
			this.setLastName((String) value);
			return;
		}
		if (TITLE.equals(attributeName))
		{
			this.setTitle((String) value);
			return;
		}
		if (AFFILIATION.equals(attributeName))
		{
			this.setAffiliation((String) value);
			return;
		}
		if (DEPARTMENT.equals(attributeName))
		{
			this.setDepartment((String) value);
			return;
		}
		if (ROLE.equals(attributeName))
		{
			this.setRole((String) value);
			return;
		}
		if (ADDRESS.equals(attributeName))
		{
			this.setAddress((String) value);
			return;
		}
		if (PHONE.equals(attributeName))
		{
			this.setPhone((String) value);
			return;
		}
		if (EMAIL.equals(attributeName))
		{
			this.setEmail((String) value);
			return;
		}
		if (FAX.equals(attributeName))
		{
			this.setFax((String) value);
			return;
		}
		if (TOLLFREEPHONE.equals(attributeName))
		{
			this.setTollFreePhone((String) value);
			return;
		}
		if (CITY.equals(attributeName))
		{
			this.setCity((String) value);
			return;
		}
		if (COUNTRY.equals(attributeName))
		{
			this.setCountry((String) value);
			return;
		}
		if (CHANGE_PASSWORD.equals(attributeName))
		{
			this.setChangePassword((Boolean) value);
			return;
		}
		if (LANGUAGECODE.equals(attributeName))
		{
			this.setLanguageCode((String) value);
			return;
		}
		if (GOOGLEACCOUNTID.equals(attributeName))
		{
			this.setGoogleAccountId((String) value);
			return;
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		MolgenisUser other = (MolgenisUser) obj;
		if (username == null)
		{
			if (other.username != null) return false;
		}
		else if (!username.equals(other.username)) return false;
		if (email == null)
		{
			if (other.email != null) return false;
		}
		else if (!email.equals(other.email)) return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		return result;
	}

	@Override
	public org.molgenis.data.EntityMetaData getEntityMetaData()
	{
		return new MolgenisUserMetaData();
	}
}
