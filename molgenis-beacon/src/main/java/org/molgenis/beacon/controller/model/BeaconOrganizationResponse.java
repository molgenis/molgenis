package org.molgenis.beacon.controller.model;

import com.google.auto.value.AutoValue;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.gson.AutoGson;

/** Organization owning a beacon. */
@AutoValue
@AutoGson(autoValueClass = AutoValue_BeaconOrganizationResponse.class)
@SuppressWarnings("java:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class BeaconOrganizationResponse {
  /** Unique identifier of the organization. */
  public abstract String getId();

  /** Name of the organization. */
  public abstract String getName();

  /** Description of the organization. */
  @Nullable
  @CheckForNull
  public abstract String getDescription();

  /** Address of the organization. */
  @Nullable
  @CheckForNull
  public abstract String getAddress();

  /** URL of the website of the organization (RFC 3986 format). */
  @Nullable
  @CheckForNull
  public abstract String getWelcomeUrl();

  /**
   * URL with the contact for the beacon operator/maintainer, e.g. link to a contact form (RFC 3986
   * format) or an email (RFC 2368 format).
   */
  @Nullable
  @CheckForNull
  public abstract String getContactUrl();

  /** URL to the logo (PNG/JPG format) of the organization (RFC 3986 format). */
  @Nullable
  @CheckForNull
  public abstract String getLogoUrl();

  public static BeaconOrganizationResponse create(
      String id,
      String name,
      String description,
      String address,
      String welcomeUrl,
      String contactUrl,
      String logoUrl) {
    return new AutoValue_BeaconOrganizationResponse(
        id, name, description, address, welcomeUrl, contactUrl, logoUrl);
  }
}
