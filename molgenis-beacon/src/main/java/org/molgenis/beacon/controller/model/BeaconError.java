package org.molgenis.beacon.controller.model;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

/**
 * BeaconResponse-specific error representing an unexpected problem.
 */
@AutoValue
@AutoGson(autoValueClass = AutoValue_BeaconError.class)
public abstract class BeaconError
{
	/**
	 * Numeric error code.
	 */
	public abstract Integer getErrorCode();

	/**
	 * Error message.
	 */
	public abstract String getMessage();

	public static BeaconError create(Integer errorCode, String message)
	{
		return new AutoValue_BeaconError(errorCode, message);
	}
}
