package org.molgenis.web.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.HandlerMethod;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ControllerAdvice
@Order
public class FallbackExceptionHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(FallbackExceptionHandler.class);

	@Value("${environment:production}")
	private String environment;

	@ResponseStatus(INTERNAL_SERVER_ERROR)
	@ExceptionHandler
	public Object handleException(Exception e, HandlerMethod handlerMethod)
	{
		LOG.error("", e);
		return ExceptionHandlerUtils.handleException(e, handlerMethod, INTERNAL_SERVER_ERROR, environment, null);
	}
}
