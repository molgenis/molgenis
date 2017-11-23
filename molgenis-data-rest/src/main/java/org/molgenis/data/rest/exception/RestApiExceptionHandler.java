package org.molgenis.data.rest.exception;

import org.molgenis.web.exception.BadRequestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.molgenis.web.exception.ExceptionHandlerUtils.handleTypedException;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ControllerAdvice
public class RestApiExceptionHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(RestApiExceptionHandler.class);

	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler
	public Object handleRestApiException(RestApiException e)
	{
		LOG.info(e.getErrorCode(), e);
		return handleTypedException(false, BadRequestController.URI, e.getLocalizedMessage(), BAD_REQUEST,
				e.getErrorCode());
	}
}
