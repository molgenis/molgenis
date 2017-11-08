package org.molgenis.fair.controller;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ControllerAdvice
public class FairExceptionHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(FairExceptionHandler.class);

	@ExceptionHandler(UnknownCatalogException.class)
	@ResponseBody
	@ResponseStatus(BAD_REQUEST)
	public Model handleUnknownCatalogException(UnknownCatalogException e)
	{
		LOG.warn(e.getMessage(), e);
		return new LinkedHashModel();
	}

	@ExceptionHandler(UnknownDatasetException.class)
	@ResponseBody
	@ResponseStatus(BAD_REQUEST)
	public Model handleUnknownDatasetException(UnknownDatasetException e)
	{
		LOG.warn(e.getMessage(), e);
		return new LinkedHashModel();
	}

	@ExceptionHandler(UnknownDistributionException.class)
	@ResponseBody
	@ResponseStatus(BAD_REQUEST)
	public Model handleUnknownDistributionException(UnknownDistributionException e)
	{
		LOG.warn(e.getMessage(), e);
		return new LinkedHashModel();
	}
}
