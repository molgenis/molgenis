package org.molgenis.beacon.controller.model;

import com.google.auto.value.AutoValue;
import org.molgenis.core.gson.AutoGson;

import javax.annotation.Nullable;

/**
 * Organization owning a beacon.
 */
@AutoValue
@AutoGson(autoValueClass = AutoValue_BeaconOrganizationResponse.class)
public abstract class BeaconOrganizationResponse
{
	/**
	 * Unique identifier of the organization.
	 */
	public abstract String getId();

	/**
	 * Name of the organization.
	 */
	public abstract String getName();

	/**
	 * Description of the organization.
	 */
	@Nullable
	public abstract String getDescription();

	/**
	 * Address of the organization.
	 */
	@Nullable
	public abstract String getAddress();

	/**
	 * URL of the website of the organization (RFC 3986 format).
	 */
	@Nullable
	public abstract String getWelcomeUrl();

	/**
	 * URL with the contact for the beacon operator/maintainer, e.g. link to
	 * a contact form (RFC 3986 format) or an email (RFC 2368 format).
	 */
	@Nullable
	public abstract String getContactUrl();

	/**
	 * URL to the logo (PNG/JPG format) of the organization (RFC 3986 format).
	 */
	@Nullable
	public abstract String getLogoUrl();

	public static BeaconOrganizationResponse create(String id, String name, String description, String address,
			String welcomeUrl, String contactUrl, String logoUrl)
	{
		return new AutoValue_BeaconOrganizationResponse(id, name, description, address, welcomeUrl, contactUrl, logoUrl);
	}
}