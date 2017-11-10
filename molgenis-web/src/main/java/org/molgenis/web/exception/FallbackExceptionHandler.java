package org.molgenis.web.exception;

import org.molgenis.web.ErrorMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ControllerAdvice
@Order
public class FallbackExceptionHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(FallbackExceptionHandler.class);

	@ResponseStatus(INTERNAL_SERVER_ERROR)
	@ExceptionHandler
	public Object handleException(Exception e, HandlerMethod handlerMethod)
	{
		LOG.error("", e);
		if (isHtmlRequest(handlerMethod))
		{
			return "forward:" + InternalServerErrorController.URI;
		}
		else
		{
			ErrorMessageResponse errorMessageResponse = ErrorMessageResponse.create(
					InternalServerErrorController.ERROR_MESSAGE);
			return new ResponseEntity<>(errorMessageResponse, INTERNAL_SERVER_ERROR);
		}
	}

	// TODO get rid of code duplication with GlobalControllerExceptionHandler
	private boolean isHtmlRequest(HandlerMethod handlerMethod)
	{
		return !(handlerMethod.hasMethodAnnotation(ResponseBody.class) || handlerMethod.hasMethodAnnotation(
				ResponseStatus.class) || handlerMethod.getBeanType().isAnnotationPresent(ResponseBody.class)
				|| handlerMethod.getBeanType().isAnnotationPresent(RestController.class));
	}
}
