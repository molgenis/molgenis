package org.molgenis.beacon.controller.model;

import com.google.auto.value.AutoValue;
import org.molgenis.beacon.config.Beacon;
import org.molgenis.util.AutoGson;

import javax.annotation.Nullable;

/**
 * BeaconResponse's response to a query for information about a specific allele.
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
	@Nullable
	public abstract Boolean getExists();

	/**
	 * BeaconResponse-specific error.
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

	public static BeaconAlleleResponse create(String beaconId, Boolean exists, BeaconError error,
			BeaconAlleleRequest alleleRequest)
	{
		return new AutoValue_BeaconAlleleResponse(beaconId, exists, error, alleleRequest);
	}
}
