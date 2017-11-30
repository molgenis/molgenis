package org.molgenis.fair.controller;

import static java.util.Objects.requireNonNull;

class UnknownCatalogException extends FairException
{
	private static final String MESSAGE_FORMAT = "Catalog with id [%s] does not exist";

	private final String catalogId;

	UnknownCatalogException(String catalogId)
	{
		this.catalogId = requireNonNull(catalogId);
	}

	public String getCatalogId()
	{
		return catalogId;
	}

	@Override
	public String getMessage()
	{
		return String.format(MESSAGE_FORMAT, catalogId);
	}
}
