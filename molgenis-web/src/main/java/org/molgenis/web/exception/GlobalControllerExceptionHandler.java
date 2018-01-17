package org.molgenis.web.exception;

import org.molgenis.data.UnknownDataException;
import org.molgenis.data.security.exception.PermissionDeniedException;
import org.molgenis.data.validation.DataIntegrityViolationException;
import org.molgenis.data.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.HandlerMethod;

import static org.molgenis.web.exception.ExceptionHandlerUtils.handleException;
import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalControllerExceptionHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

	@Value("${environment:production}")
	private String environment;

	@ExceptionHandler
	public Object handleUnknownDataException(UnknownDataException e, HandlerMethod handlerMethod)
	{
		LOG.info(e.getErrorCode(), e);
		return handleException(e, handlerMethod, BAD_REQUEST, e.getErrorCode(), environment);
	}

	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler
	public Object handleDataIntegrityViolationException(DataIntegrityViolationException e, HandlerMethod handlerMethod)
	{
		LOG.info(e.getErrorCode(), e);
		return handleException(e, handlerMethod, BAD_REQUEST, e.getErrorCode(), environment);
	}

	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler
	public Object handleValidationException(ValidationException e, HandlerMethod handlerMethod)
	{
		LOG.info(e.getErrorCode(), e);
		return handleException(e, handlerMethod, BAD_REQUEST, e.getErrorCode(), environment);
	}

	@ExceptionHandler
	public Object handlePermissionDeniedException(PermissionDeniedException e, HandlerMethod handlerMethod)
	{
		LOG.info(e.getErrorCode(), e);
		return handleException(e, handlerMethod, FORBIDDEN, e.getErrorCode(), environment);
	}
}
