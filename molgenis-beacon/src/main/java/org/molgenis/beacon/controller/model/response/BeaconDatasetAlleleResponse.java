package org.molgenis.beacon.controller.model.response;

import com.google.auto.value.AutoValue;
import org.molgenis.beacon.controller.model.BeaconError;
import org.molgenis.gson.AutoGson;

import java.util.Map;

/**
 * Dataset's response to a query for information about a specific allele.
 */
@AutoValue
@AutoGson(autoValueClass = AutoValue_BeaconDatasetAlleleResponse.class)
public abstract class BeaconDatasetAlleleResponse
{
	/**
	 * Identifier of the dataset, as defined in `BeaconDataset`.
	 */
	public abstract String getDatasetId();

	/**
	 * Indicator of whether the given allele was observed in the dataset.
	 * <p>
	 * This should be non-null, unless there was an error, in which case
	 * `error` has to be non-null.
	 */
	public abstract boolean getExists();

	/**
	 * Dataset-specific error.
	 * <p>
	 * This should be non-null in exceptional situations only, in which case
	 * `exists` has to be null.
	 */
	public abstract BeaconError getError();

	/**
	 * Frequency of this allele in the dataset. Between 0 and 1, inclusive.
	 */
	public abstract Double getFrequency();

	/**
	 * Number of variants matching the allele request in the dataset.
	 */
	public abstract Long getVariantCount();

	/**
	 * Number of calls matching the allele request in the dataset.
	 */
	public abstract Long getCallCount();

	/**
	 * Number of samples matching the allele request in the dataset.
	 */
	public abstract Long getSampleCount();

	/**
	 * Additional note or description of the response.
	 */
	public abstract String getNote();

	/**
	 * URL to an external system, such as a secured beacon or a system providing
	 * more information about a given allele (RFC 3986 format).
	 */
	public abstract String getExternalUrl();

	/**
	 * Additional structured metadata, key-value pairs.
	 */
	public abstract Map<String, String> getInfo();

	public static BeaconDatasetAlleleResponse create(String datasetId, boolean exists, BeaconError error,
			Double frequency, Long variantCount, Long callCount, Long sampleCount, String note, String externalUrl,
			Map<String, String> info)
	{
		return new AutoValue_BeaconDatasetAlleleResponse(datasetId, exists, error, frequency, variantCount, callCount,
				sampleCount, note, externalUrl, info);
	}
}
