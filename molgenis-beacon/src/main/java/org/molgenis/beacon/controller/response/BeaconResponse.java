package org.molgenis.beacon.controller.response;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_BeaconResponse.class)
public abstract class BeaconResponse
{
	public abstract String getBeaconId();

	public abstract boolean getExists();

	public static BeaconResponse create(String beaconId, boolean exisits)
	{
		return new AutoValue_BeaconResponse(beaconId, exisits);
	}
}
