package org.molgenis.beacon.controller.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import java.util.Map;

/**
 * Dataset of a beacon.
 */
@AutoValue
@AutoGson(autoValueClass = AutoValue_BeaconDataset.class)
public abstract class BeaconDataset
{
	/**
	 * Unique identifier of the dataset.
	 */
	public abstract String getId();

	/**
	 * Name of the dataset.
	 */
	public abstract String getName();

	/**
	 * Description of the dataset.
	 */
	public abstract String getDescription();

	/**
	 * Assembly identifier (GRC notation, e.g. `GRCh37`).
	 */
	public abstract String getAssemblyId();

	/**
	 * The time the dataset was created (ISO 8601 format).
	 */
	public abstract String getCreateDateTime();

	/**
	 * The time the dataset was updated in (ISO 8601 format).
	 */
	public abstract String getUpdateDateTime();

	/**
	 * Version of the dataset.
	 */
	public abstract String getVersion();

	/**
	 * Total number of variants in the dataset.
	 */
	public abstract Long getVariantCount();

	/**
	 * Total number of calls in the dataset.
	 */
	public abstract Long getCallCount();

	/**
	 * Total number of samples in the dataset.
	 */
	public abstract Long getSampleCount();

	/**
	 * URL to an external system providing more dataset information (RFC 3986 format).
	 */
	public abstract String getExternalUrl();

	/**
	 * Additional structured metadata, key-value pairs.
	 */
	public abstract Map<String, String> getInfo();

	public static BeaconDataset create(String id, String name, String description, String assemblyId,
			String createDateTime, String updateDateTime, String version, Long variantCount, Long callCount,
			Long sampleCount, String externalUrl, Map<String, String> info)
	{
		return new AutoValue_BeaconDataset(id, name, description, assemblyId, createDateTime, updateDateTime, version,
				variantCount, callCount, sampleCount, externalUrl, info);
	}
}
