package org.molgenis.beacon.model.exceptions;

import org.molgenis.beacon.controller.model.BeaconAlleleRequest;

import static java.util.Objects.requireNonNull;

public class NestedBeaconException extends BeaconException
{
	private String beaconId;
	private BeaconAlleleRequest request;

	public NestedBeaconException(String beaconId, BeaconAlleleRequest request)
	{
		this.beaconId = requireNonNull(beaconId);
		this.request = requireNonNull(request);
	}

	public BeaconAlleleRequest getRequest()
	{
		return request;
	}

	public String getBeaconId()
	{
		return beaconId;
	}
}
