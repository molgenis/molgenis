package org.molgenis.beacon.model.exceptions;

import org.molgenis.beacon.controller.model.BeaconAlleleRequest;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class UnknownBeaconException extends BeaconException
{
	private String beaconId = "";
	private BeaconAlleleRequest request;

	public UnknownBeaconException(String beaconId, BeaconAlleleRequest request)
	{
		super(format("Unknown beacon [%s]", beaconId));
		this.beaconId = requireNonNull(beaconId);
		this.request = requireNonNull(request);
	}

	public String getBeaconId()
	{
		return beaconId;
	}

	public BeaconAlleleRequest getRequest()
	{
		return request;
	}
}
