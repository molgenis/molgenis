package org.molgenis.fair.controller;

import static java.util.Objects.requireNonNull;

public class UnknownDatasetException extends FairException
{
	private static final String MESSAGE_FORMAT = "Dataset with id [%s] does not exist";

	private final String datasetId;

	public UnknownDatasetException(String datasetId)
	{
		this.datasetId = requireNonNull(datasetId);
	}

	@Override
	public String getMessage()
	{
		return String.format(MESSAGE_FORMAT, datasetId);
	}
}
