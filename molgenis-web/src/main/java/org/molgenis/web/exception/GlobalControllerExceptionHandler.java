package org.molgenis.web.exception;

import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.UnknownEntityTypeException;
import org.molgenis.web.ErrorMessageResponse;
import org.molgenis.web.PluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

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
		if (isHtmlRequest(httpServletRequest))
		{
			return "forward:" + NotFoundController.URI;
		}
		else
		{
			ErrorMessageResponse errorMessageResponse = ErrorMessageResponse.create(NotFoundController.ERROR_MESSAGE);
			return new ResponseEntity<>(errorMessageResponse, NOT_FOUND);
		}
	}

	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler
	public Object handleUnknownEntityException(UnknownEntityException e, HandlerMethod handlerMethod)
	{
		LOG.warn("", e);
		if (isHtmlRequest(handlerMethod))
		{
			return "forward:" + NotFoundController.URI;
		}
		else
		{
			//FIXME: handle cases where the key does not exist in the resourceBundle
			ErrorMessageResponse errorMessageResponse = ErrorMessageResponse.create(e.getLocalizedMessage());
			return new ResponseEntity<>(errorMessageResponse, BAD_REQUEST);
		}
	}

	@ResponseStatus(BAD_REQUEST)
	@ExceptionHandler
	public Object handleUnknownEntityTypeException(UnknownEntityTypeException e, HandlerMethod handlerMethod)
	{
		LOG.warn("", e);
		if (isHtmlRequest(handlerMethod))
		{
			return "forward:" + NotFoundController.URI;
		}
		else
		{
			//FIXME: handle cases where the key does not exist in the resourceBundle
			ErrorMessageResponse errorMessageResponse = ErrorMessageResponse.create(e.getLocalizedMessage());
			return new ResponseEntity<>(errorMessageResponse, BAD_REQUEST);
		}
	}

	//TODO: MolgenisDataException
	//TODO: MolgenisPermissionException
	//TODO: MolgenisRuntimeException
	//TODO: MolgenisValidationException
	//TODO: Exception -> who wins this, specific FAIR handler or the global one

	private boolean isHtmlRequest(HttpServletRequest httpServletRequest)
	{
		try
		{
			URL url = new URL(httpServletRequest.getRequestURL().toString());
			return url.getPath().startsWith(PluginController.PLUGIN_URI_PREFIX);
		}
		catch (MalformedURLException e)
		{
			LOG.error("", e);
			return false;
		}
	}

	private boolean isHtmlRequest(HandlerMethod handlerMethod)
	{
		return !(handlerMethod.hasMethodAnnotation(ResponseBody.class) || handlerMethod.hasMethodAnnotation(
				ResponseStatus.class) || handlerMethod.getBeanType().isAnnotationPresent(ResponseBody.class)
				|| handlerMethod.getBeanType().isAnnotationPresent(RestController.class));
	}
}
