package org.molgenis.beacon.controller.model.exceptions;

import org.molgenis.util.exception.CodedRuntimeException;

/**
 * Beacon exception that can be handled by the {@link BeaconExceptionHandler}
 *
 * @deprecated use class that extends from {@link CodedRuntimeException}
 */
@Deprecated
public abstract class BeaconException extends RuntimeException {
  public BeaconException() {}

  public BeaconException(String message) {
    super(message);
  }
}
