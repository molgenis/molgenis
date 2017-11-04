package org.molgenis.data.security.model;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;
import org.molgenis.security.core.model.User;

import javax.annotation.Nullable;
import java.util.Optional;

import static org.molgenis.data.security.model.UserMetadata.*;

public class UserEntity extends StaticEntity
{
	public UserEntity(Entity entity)
	{
		super(entity);
	}

	public UserEntity(EntityType entityType)
	{
		super(entityType);
	}

	public UserEntity(String id, EntityType entityType)
	{
		super(entityType);
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

	@Nullable
	public String getActivationCode()
	{
		return getString(ACTIVATIONCODE);
	}

	public void setActivationCode(String activationCode)
	{
		set(ACTIVATIONCODE, activationCode);
	}

	public boolean isTwoFactorAuthentication()
	{
		return Optional.ofNullable(getBoolean(TWO_FACTOR_AUTHENTICATION)).orElse(false);
	}

	public void setTwoFactorAuthentication(boolean twoFactorAuthentication)
	{
		set(TWO_FACTOR_AUTHENTICATION, twoFactorAuthentication);
	}

	public boolean isActive()
	{
		return Optional.ofNullable(getBoolean(ACTIVE)).orElse(false);
	}

	public void setActive(Boolean active)
	{
		set(ACTIVE, active);
	}

	public boolean isSuperuser()
	{
		return Optional.ofNullable(getBoolean(SUPERUSER)).orElse(false);
	}

	public void setSuperuser(Boolean superuser)
	{
		set(SUPERUSER, superuser);
	}

	@Nullable
	public String getFirstName()
	{
		return getString(FIRSTNAME);
	}

	public void setFirstName(String firstName)
	{
		set(FIRSTNAME, firstName);
	}

	@Nullable
	public String getMiddleNames()
	{
		return getString(MIDDLENAMES);
	}

	public void setMiddleNames(String middleNames)
	{
		set(MIDDLENAMES, middleNames);
	}

	@Nullable
	public String getLastName()
	{
		return getString(LASTNAME);
	}

	public void setLastName(String lastName)
	{
		set(LASTNAME, lastName);
	}

	@Nullable
	public String getTitle()
	{
		return getString(TITLE);
	}

	public void setTitle(String title)
	{
		set(TITLE, title);
	}

	@Nullable
	public String getAffiliation()
	{
		return getString(AFFILIATION);
	}

	public void setAffiliation(String affiliation)
	{
		set(AFFILIATION, affiliation);
	}

	@Nullable
	public String getDepartment()
	{
		return getString(DEPARTMENT);
	}

	public void setDepartment(String department)
	{
		set(DEPARTMENT, department);
	}

	@Nullable
	public String getRole()
	{
		return getString(ROLE);
	}

	public void setRole(String role)
	{
		set(ROLE, role);
	}

	@Nullable
	public String getAddress()
	{
		return getString(ADDRESS);
	}

	public void setAddress(String address)
	{
		set(ADDRESS, address);
	}

	@Nullable
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

	@Nullable
	public String getFax()
	{
		return getString(FAX);
	}

	public void setFax(String fax)
	{
		set(FAX, fax);
	}

	@Nullable
	public String getTollFreePhone()
	{
		return getString(TOLLFREEPHONE);
	}

	public void setTollFreePhone(String tollFreePhone)
	{
		set(TOLLFREEPHONE, tollFreePhone);
	}

	@Nullable
	public String getCity()
	{
		return getString(CITY);
	}

	public void setCity(String city)
	{
		set(CITY, city);
	}

	@Nullable
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
		return Optional.ofNullable(getBoolean(CHANGE_PASSWORD)).orElse(false);
	}

	public void setChangePassword(Boolean changePassword)
	{
		set(CHANGE_PASSWORD, changePassword);
	}

	@Nullable
	public String getLanguageCode()
	{
		return getString(LANGUAGECODE);
	}

	public void setLanguageCode(String languageCode)
	{
		set(LANGUAGECODE, languageCode);
	}

	@Nullable
	public String getGoogleAccountId()
	{
		return getString(GOOGLEACCOUNTID);
	}

	public void setGoogleAccountId(String googleAccountId)
	{
		set(GOOGLEACCOUNTID, googleAccountId);
	}

	public User toUser()
	{
		User.Builder result = User.builder()
								  .username(getUsername())
								  .password(getPassword())
								  .email(getEmail())
								  .twoFactorAuthentication(isTwoFactorAuthentication())
								  .active(isActive())
								  .superuser(isSuperuser())
								  .changePassword(isChangePassword());
		Optional.ofNullable(getId()).ifPresent(result::id);
		Optional.ofNullable(getActivationCode()).ifPresent(result::activationCode);
		Optional.ofNullable(getFirstName()).ifPresent(result::firstName);
		Optional.ofNullable(getMiddleNames()).ifPresent(result::middleNames);
		Optional.ofNullable(getLastName()).ifPresent(result::lastName);
		Optional.ofNullable(getTitle()).ifPresent(result::title);
		Optional.ofNullable(getAffiliation()).ifPresent(result::affiliation);
		Optional.ofNullable(getDepartment()).ifPresent(result::department);
		Optional.ofNullable(getAddress()).ifPresent(result::address);
		Optional.ofNullable(getPhone()).ifPresent(result::phone);
		Optional.ofNullable(getFax()).ifPresent(result::fax);
		Optional.ofNullable(getTollFreePhone()).ifPresent(result::tollFreePhone);
		Optional.ofNullable(getCity()).ifPresent(result::city);
		Optional.ofNullable(getCountry()).ifPresent(result::country);
		Optional.ofNullable(getLanguageCode()).ifPresent(result::languageCode);
		Optional.ofNullable(getGoogleAccountId()).ifPresent(result::googleAccountId);
		return result.build();
	}

	public UserEntity updateFrom(User user)
	{
		setUsername(user.getUsername());
		setPassword(user.getPassword());
		setEmail(user.getEmail());
		setTwoFactorAuthentication(user.isTwoFactorAuthentication());
		setActive(user.isActive());
		setSuperuser(user.isSuperuser());
		setChangePassword(user.isChangePassword());
		user.getId().ifPresent(this::setId);
		user.getActivationCode().ifPresent(this::setActivationCode);
		user.getFirstName().ifPresent(this::setFirstName);
		user.getMiddleNames().ifPresent(this::setMiddleNames);
		user.getLastName().ifPresent(this::setLastName);
		user.getTitle().ifPresent(this::setTitle);
		user.getAffiliation().ifPresent(this::setAffiliation);
		user.getDepartment().ifPresent(this::setDepartment);
		user.getAddress().ifPresent(this::setAddress);
		user.getPhone().ifPresent(this::setPhone);
		user.getFax().ifPresent(this::setFax);
		user.getTollFreePhone().ifPresent(this::setTollFreePhone);
		user.getCity().ifPresent(this::setCity);
		user.getCountry().ifPresent(this::setCountry);
		user.getLanguageCode().ifPresent(this::setLanguageCode);
		user.getGoogleAccountId().ifPresent(this::setGoogleAccountId);
		return this;
	}
}
