package org.molgenis.beacon.service;

import java.util.List;
import org.molgenis.beacon.config.Beacon;
import org.molgenis.beacon.controller.model.BeaconResponse;

public interface BeaconInfoService {
  /**
   * Fetch all available Beacons
   *
   * @return A list of beacons
   */
  List<BeaconResponse> getAvailableBeacons();

  /**
   * Fetch information on a specific beacon
   *
   * @param beaconId
   * @return A {@link Beacon}
   */
  BeaconResponse info(String beaconId);
}
