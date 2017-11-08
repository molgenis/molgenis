package org.molgenis.fair.controller;

import static java.util.Objects.requireNonNull;

class UnknownCatalogException extends RuntimeException
{
	private static final String MESSAGE_FORMAT = "Catalog with id [%s] does not exist";

	private final String catalogId;

	UnknownCatalogException(String catalogId)
	{
		this.catalogId = requireNonNull(catalogId);
	}

	@Override
	public String getMessage()
	{
		return String.format(MESSAGE_FORMAT, catalogId);
	}
}
