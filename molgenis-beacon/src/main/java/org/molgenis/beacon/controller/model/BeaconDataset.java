package org.molgenis.beacon.controller.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import javax.annotation.Nullable;

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
	@Nullable
	public abstract String getDescription();

	public static BeaconDataset create(String id, String name, String description)
	{
		return new AutoValue_BeaconDataset(id, name, description);
	}
}
