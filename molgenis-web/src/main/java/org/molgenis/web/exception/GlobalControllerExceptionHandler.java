package org.molgenis.web.exception;

import com.google.common.collect.Lists;
import org.molgenis.data.UnknownDataException;
import org.molgenis.data.security.exception.EntityTypePermissionDeniedException;
import org.molgenis.data.validation.DataIntegrityViolationException;
import org.molgenis.data.validation.ValidationException;
import org.molgenis.web.ErrorMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static org.molgenis.web.exception.ExceptionHandlerUtils.handleTypedException;
import static org.molgenis.web.exception.ExceptionHandlerUtils.isHtmlRequest;
import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalControllerExceptionHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

	@ResponseStatus(NOT_FOUND)
	@ExceptionHandler(NoHandlerFoundException.class)
	public Object handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest httpServletRequest)
	{
		LOG.info("", e);
		return handleTypedException(isHtmlRequest(httpServletRequest), BadRequestController.URI,
				e.getLocalizedMessage(),
				NOT_FOUND);
	}

	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public Object handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e)
	{
		LOG.info("", e);
		return handleTypedException(false, BadRequestController.URI, e.getLocalizedMessage(), BAD_REQUEST);
	}

	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler
	public Object handleUnknownDataException(UnknownDataException e, HandlerMethod handlerMethod)
	{
		LOG.info(e.getErrorCode(), e);
		return handleTypedException(isHtmlRequest(handlerMethod), BadRequestController.URI, e.getLocalizedMessage(),
				BAD_REQUEST, e.getErrorCode());
	}

	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler
	public Object handleDataIntegrityViolationException(DataIntegrityViolationException e, HandlerMethod handlerMethod)
	{
		LOG.info(e.getErrorCode(), e);
		return handleTypedException(isHtmlRequest(handlerMethod), BadRequestController.URI, e.getLocalizedMessage(),
				BAD_REQUEST, e.getErrorCode());
	}

	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler
	public Object handleValidationException(ValidationException e, HandlerMethod handlerMethod)
	{
		LOG.info(e.getErrorCode(), e);
		return handleTypedException(isHtmlRequest(handlerMethod), BadRequestController.URI, e.getLocalizedMessage(),
				BAD_REQUEST, e.getErrorCode());
	}

	@ResponseStatus(FORBIDDEN)
	@ExceptionHandler
	public Object handlePermissionDeniedException(EntityTypePermissionDeniedException e, HandlerMethod handlerMethod)
	{
		LOG.info(e.getErrorCode(), e);
		return handleTypedException(isHtmlRequest(handlerMethod), BadRequestController.URI, e.getLocalizedMessage(),
				FORBIDDEN, e.getErrorCode()); // FIXME NotFoundController.URI is not what we want here (?)
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public ErrorMessageResponse handleHttpMessageNotReadableException(HttpMessageNotReadableException e)
	{
		LOG.error("", e);
		return new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage(e.getLocalizedMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(BAD_REQUEST)
	@ResponseBody
	public ErrorMessageResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e)
	{
		LOG.debug("", e);
		List<ErrorMessageResponse.ErrorMessage> messages = Lists.newArrayList();
		for (ObjectError error : e.getBindingResult().getAllErrors())
		{
			messages.add(new ErrorMessageResponse.ErrorMessage(error.getDefaultMessage()));
		}

		return new ErrorMessageResponse(messages);
	}
}
