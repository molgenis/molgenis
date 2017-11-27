package org.molgenis.beacon.controller.model.response;

import com.google.auto.value.AutoValue;
import org.molgenis.beacon.controller.model.Beacon;
import org.molgenis.beacon.controller.model.BeaconError;
import org.molgenis.beacon.controller.model.request.BeaconAlleleRequest;
import org.molgenis.gson.AutoGson;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Beacon's response to a query for information about a specific allele.
 */
@AutoValue
@AutoGson(autoValueClass = AutoValue_BeaconAlleleResponse.class)
public abstract class BeaconAlleleResponse
{
	/**
	 * Identifier of the beacon, as defined in @{@link Beacon}.
	 */
	public abstract String getBeaconId();

	/**
	 * Indicator of whether the given allele was observed in any of the datasets
	 * queried.
	 * <p>
	 * This should be non-null, unless there was an error, in which case
	 * `error` has to be non-null.
	 */
	public abstract boolean getExists();

	/**
	 * Beacon-specific error.
	 * <p>
	 * This should be non-null in exceptional situations only, in which case
	 * `exists` has to be null.
	 */
	@Nullable
	public abstract BeaconError getError();

	/**
	 * Allele request as interpreted by the beacon.
	 */
	public abstract BeaconAlleleRequest getAlleleRequest();

	/**
	 * Indicator of whether the given allele was observed in individual datasets.
	 * <p>
	 * This should be non-null if `includeDatasetResponses` in the corresponding {@link BeaconAlleleRequest} is true, and null otherwise.
	 */
	public abstract List<BeaconDatasetAlleleResponse> getDatasetAlleleResponses();

	public static BeaconAlleleResponse create(String beaconId, boolean exists, BeaconError error,
			BeaconAlleleRequest alleleRequest, List<BeaconDatasetAlleleResponse> datasetAlleleResponses)
	{
		return new AutoValue_BeaconAlleleResponse(beaconId, exists, error, alleleRequest, datasetAlleleResponses);
	}
}
