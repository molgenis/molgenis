package org.molgenis.beacon.service;

import org.molgenis.beacon.config.Beacon;

import java.util.List;

public interface BeaconInfoService
{
	/**
	 * Fetch all available Beacons
	 *
	 * @return A list of beacons
	 */
	List<Beacon> getAvailableBeacons();

	/**
	 * Fetch information on a specific beacon
	 *
	 * @param beaconId
	 * @return A {@link Beacon}
	 */
	Beacon info(String beaconId);
}
