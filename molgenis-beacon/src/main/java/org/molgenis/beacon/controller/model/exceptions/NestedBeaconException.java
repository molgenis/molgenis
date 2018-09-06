package org.molgenis.beacon.controller.model.exceptions;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import org.molgenis.beacon.controller.model.BeaconAlleleRequest;

/** @deprecated use class that extends from {@link org.molgenis.i18n.CodedRuntimeException} */
@Deprecated
public class NestedBeaconException extends BeaconException {
  private String beaconId;
  private BeaconAlleleRequest request;

  public NestedBeaconException(String beaconId, BeaconAlleleRequest request) {
    super(format("Some error occured when querying [%s]", beaconId));
    this.beaconId = requireNonNull(beaconId);
    this.request = requireNonNull(request);
  }

  public BeaconAlleleRequest getRequest() {
    return request;
  }

  public String getBeaconId() {
    return beaconId;
  }
}
