package org.molgenis.security.core.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents a User.
 */
@AutoValue
@AutoGson(autoValueClass = AutoValue_User.class)
@SuppressWarnings("squid:S1610")
public abstract class User
{
	public abstract Optional<String> getId();

	public abstract String getUsername();

	public abstract String getPassword();

	public abstract String getEmail();

	public abstract boolean isTwoFactorAuthentication();

	public abstract boolean isActive();

	public abstract boolean isSuperuser();
	
	public abstract Optional<String> getActivationCode();

	public abstract Optional<String> getFirstName();

	public abstract Optional<String> getMiddleNames();

	public abstract Optional<String> getLastName();

	public abstract Optional<String> getTitle();

	public abstract Optional<String> getAffiliation();

	public abstract Optional<String> getDepartment();

	public abstract Optional<String> getAddress();

	public abstract Optional<String> getPhone();

	public abstract Optional<String> getFax();

	public abstract Optional<String> getTollFreePhone();

	public abstract Optional<String> getCity();

	// TODO: is this duplicate with the whole Role/Group administration?
	public abstract Optional<String> getRole();

	public abstract Optional<String> getCountry();

	public abstract boolean isChangePassword();

	public abstract Optional<String> getLanguageCode();

	public abstract Optional<String> getGoogleAccountId();

	/**
	 * Formats the User's name.
	 *
	 * @return String containing the user's first name, middle names and last name, or the user name if none of those present.
	 */
	public String getFormattedName()
	{
		List<String> parts = new ArrayList<>();
		getTitle().ifPresent(parts::add);
		getFirstName().ifPresent(parts::add);
		getMiddleNames().ifPresent(parts::add);
		getLastName().ifPresent(parts::add);
		if (parts.isEmpty())
		{
			return getUsername();
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
