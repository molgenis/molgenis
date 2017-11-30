package org.molgenis.beacon.model.exceptions;

import org.molgenis.beacon.controller.model.BeaconAlleleRequest;

/**
 * BeaconResponse-specific error representing an unexpected problem.
 */
public class BeaconException extends RuntimeException
{

	private String beaconId = "";
	private String exceptionMessage = "";
	private BeaconAlleleRequest request;

	public String getBeaconId()
	{
		return beaconId;
	}

	public String getExceptionMessage()
	{
		return exceptionMessage;
	}

	public BeaconAlleleRequest getRequest()
	{
		return request;
	}

	public BeaconException(String beaconId, String exceptionMessage, BeaconAlleleRequest request)
	{
		this.beaconId = beaconId;
		this.exceptionMessage = exceptionMessage;
		this.request = request;
	}

}
