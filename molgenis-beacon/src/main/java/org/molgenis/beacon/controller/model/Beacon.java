package org.molgenis.beacon.controller.model;

import com.google.auto.value.AutoValue;
import org.molgenis.beacon.controller.model.request.BeaconAlleleRequest;
import org.molgenis.gson.AutoGson;

import java.util.List;
import java.util.Map;

/**
 * Beacon.
 */
@AutoValue
@AutoGson(autoValueClass = AutoValue_Beacon.class)
public abstract class Beacon
{
	/**
	 * Unique identifier of the beacon.
	 */
	public abstract String getId();

	/**
	 * Name of the beacon.
	 */
	public abstract String getName();

	/**
	 * Version of the API provided by the beacon.
	 */
	public abstract String getApiVersion();

	/**
	 * Organization owning the beacon.
	 */
	public abstract BeaconOrganization getOrganization();

	/**
	 * Description of the beacon.
	 */
	public abstract String getDescription();

	/**
	 * Version of the beacon.
	 */
	public abstract String getVersion();

	/**
	 * URL to the welcome page for this beacon (RFC 3986 format).
	 */
	public abstract String getWelcomeUrl();

	/**
	 * Alternative URL to the API, e.g. a restricted version of this beacon
	 * (RFC 3986 format).
	 */
	public abstract String getAlternativeUrl();

	/**
	 * The time the beacon was created (ISO 8601 format).
	 */
	public abstract String getCreateDateTime();

	/**
	 * The time the beacon was updated in (ISO 8601 format).
	 */
	public abstract String getUpdateDateTime();

	/**
	 * Datasets served by the beacon. Any beacon should specify at least one
	 * dataset.
	 */
	public abstract List<BeaconDataset> getDatasets();

	/**
	 * Examples of interesting queries, e.g. a few queries demonstrating different
	 * responses.
	 */
	public abstract List<BeaconAlleleRequest> getSampleAlleleRequests();

	/**
	 * Additional structured metadata, key-value pairs.
	 */
	public abstract Map<String, String> getInfo();

	public static Beacon create(String id, String name, String apiVersion, BeaconOrganization organization,
			String description, String version, String welcomeUrl, String alternativeUrl, String createDateTime,
			String updateDateTime, List<BeaconDataset> datasets, List<BeaconAlleleRequest> sampleAlleleRequests,
			Map<String, String> info)
	{
		return new AutoValue_Beacon(id, name, apiVersion, organization, description, version, welcomeUrl,
				alternativeUrl, createDateTime, updateDateTime, datasets, sampleAlleleRequests, info);
	}
}
