package org.molgenis.security.core.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a User.
 */
@AutoValue
@AutoGson(autoValueClass = AutoValue_User.class)
@SuppressWarnings("squid:S1610")
public abstract class User
{
	@Nullable
	public abstract String getId();

	public abstract String getUsername();

	public abstract String getPassword();

	@Nullable
	public abstract String getActivationCode();

	public abstract boolean isTwoFactorAuthentication();

	public abstract boolean isActive();

	public abstract boolean isSuperuser();

	@Nullable
	public abstract String getFirstName();

	@Nullable
	public abstract String getMiddleNames();

	@Nullable
	public abstract String getLastName();

	@Nullable
	public abstract String getTitle();

	@Nullable
	public abstract String getAffiliation();

	@Nullable
	public abstract String getDepartment();

	@Nullable
	public abstract String getAddress();

	@Nullable
	public abstract String getPhone();

	public abstract String getEmail();

	@Nullable
	public abstract String getFax();

	@Nullable
	public abstract String getTollFreePhone();

	@Nullable
	public abstract String getCity();

	// TODO: is this duplicate with the whole Role/Group administration?
	@Nullable
	public abstract String getRole();

	@Nullable
	public abstract String getCountry();

	public abstract boolean isChangePassword();

	@Nullable
	public abstract String getLanguageCode();

	@Nullable
	public abstract String getGoogleAccountId();

	/**
	 * Formats the User's name.
	 *
	 * @return String containing the user's first name, middle names and last name.
	 */
	@Nullable
	public String getFormattedName()
	{
		List<String> parts = new ArrayList<>();
		if (getTitle() != null)
		{
			parts.add(getTitle());
		}
		if (getFirstName() != null)
		{
			parts.add(getFirstName());
		}
		if (getMiddleNames() != null)
		{
			parts.add(getMiddleNames());
		}
		if (getLastName() != null)
		{
			parts.add(getLastName());
		}

		if (parts.isEmpty())
		{
			return null;
		}
		else
		{
			return StringUtils.collectionToDelimitedString(parts, " ");
		}
	}

	public static Builder builder()
	{
		return new org.molgenis.security.core.model.AutoValue_User.Builder().active(true)
																			.changePassword(false)
																			.twoFactorAuthentication(false)
																			.changePassword(false)
																			.superuser(false);
	}

	public static Builder builder(String username, String password, String email)
	{
		return builder().username(username).password(password).email(email);
	}

	public abstract Builder toBuilder();

	@AutoValue.Builder
	public abstract static class Builder
	{
		public abstract Builder id(String id);

		public abstract Builder username(String username);

		public abstract Builder password(String password);

		public abstract Builder activationCode(String activationCode);

		public abstract Builder twoFactorAuthentication(boolean twoFactorAuthentication);

		public abstract Builder active(boolean active);

		public abstract Builder superuser(boolean superuser);

		public abstract Builder firstName(String firstName);

		public abstract Builder middleNames(String middleNames);

		public abstract Builder lastName(String lastName);

		public abstract Builder title(String title);

		public abstract Builder affiliation(String affiliation);

		public abstract Builder department(String department);

		public abstract Builder address(String address);

		public abstract Builder phone(String phone);

		public abstract Builder email(String email);

		public abstract Builder fax(String fax);

		public abstract Builder tollFreePhone(String tollFreePhone);

		public abstract Builder city(String city);

		public abstract Builder role(String role);

		public abstract Builder country(String country);

		public abstract Builder changePassword(boolean changePassword);

		public abstract Builder languageCode(String languageCode);

		public abstract Builder googleAccountId(String googleAccountId);

		public abstract User build();
	}
}
