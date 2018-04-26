package org.molgenis.beacon.controller.model;

import com.google.auto.value.AutoValue;
import org.molgenis.core.gson.AutoGson;

import javax.annotation.Nullable;

/**
 * Dataset of a beacon.
 */
@AutoValue
@AutoGson(autoValueClass = AutoValue_BeaconDatasetResponse.class)
public abstract class BeaconDatasetResponse
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
	@Nullable
	public abstract String getDescription();

	public static BeaconDatasetResponse create(String id, String name, String description)
	{
		return new AutoValue_BeaconDatasetResponse(id, name, description);
	}
}
