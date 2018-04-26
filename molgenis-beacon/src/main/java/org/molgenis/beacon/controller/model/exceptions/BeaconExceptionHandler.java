package org.molgenis.beacon.controller.model.exceptions;

import org.molgenis.beacon.controller.model.BeaconAlleleResponse;
import org.molgenis.beacon.controller.model.BeaconError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

/**
 * Handles only the {@link BeaconException} that has to be converted to {@link BeaconAlleleResponse} to conform to Beacon network specifications
 */
@ControllerAdvice
@Order(HIGHEST_PRECEDENCE) // Take precedence over other handlers, see #7139
public class BeaconExceptionHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(BeaconExceptionHandler.class);

	@ExceptionHandler(UnknownBeaconException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public BeaconAlleleResponse handleUnknownBeaconException(UnknownBeaconException e)
	{
		LOG.info(e.getMessage(), e);
		return BeaconAlleleResponse.create(e.getBeaconId(), null,
				BeaconError.create(HttpStatus.BAD_REQUEST.value(), e.getMessage()), e.getRequest());
	}

	@ExceptionHandler(NestedBeaconException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public BeaconAlleleResponse handleNestedBeaconException(NestedBeaconException e)
	{
		LOG.info(e.getMessage(), e);
		return BeaconAlleleResponse.create(e.getBeaconId(), null,
				BeaconError.create(HttpStatus.BAD_REQUEST.value(), e.getMessage()), e.getRequest());
	}
}
