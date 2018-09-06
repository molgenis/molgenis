package org.molgenis.beacon.controller.model;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

/** A Beacon */
@AutoValue
@AutoGson(autoValueClass = AutoValue_BeaconResponse.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class BeaconResponse {
  /** Unique identifier of the beacon. */
  public abstract String getId();

  /** Name of the beacon. */
  public abstract String getName();

  /** Version of the API provided by the beacon. */
  public abstract String getApiVersion();

  /** Organization owning the beacon. */
  @Nullable
  public abstract BeaconOrganizationResponse getOrganization();

  /** Description of the beacon. */
  @Nullable
  public abstract String getDescription();

  /** Version of the beacon. */
  @Nullable
  public abstract String getVersion();

  /** URL to the welcome page for this beacon (RFC 3986 format). */
  @Nullable
  public abstract String getWelcomeUrl();

  /** Datasets served by the beacon. Any beacon should specify at least one dataset. */
  public abstract List<BeaconDatasetResponse> getDatasets();

  public static BeaconResponse create(
      String id,
      String name,
      String apiVersion,
      BeaconOrganizationResponse organization,
      String description,
      String version,
      String welcomeUrl,
      List<BeaconDatasetResponse> datasets) {
    return new AutoValue_BeaconResponse(
        id, name, apiVersion, organization, description, version, welcomeUrl, datasets);
  }
}
