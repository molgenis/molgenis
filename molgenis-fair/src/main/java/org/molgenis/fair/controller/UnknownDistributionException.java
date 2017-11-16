package org.molgenis.fair.controller;

import static java.util.Objects.requireNonNull;

public class UnknownDistributionException extends FairException
{
	private static final String MESSAGE_FORMAT = "Distribution with id [%s] does not exist";

	private final String distributionId;

	public UnknownDistributionException(String distributionId)
	{
		this.distributionId = requireNonNull(distributionId);
	}

	@Override
	public String getMessage()
	{
		return String.format(MESSAGE_FORMAT, distributionId);
	}
}
