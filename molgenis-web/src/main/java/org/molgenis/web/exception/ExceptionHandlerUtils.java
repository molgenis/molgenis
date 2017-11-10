package org.molgenis.web.exception;

import org.molgenis.web.ErrorMessageResponse;
import org.molgenis.web.PluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;

public class ExceptionHandlerUtils
{
	private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandlerUtils.class);

	public static Object handleTypedException(Exception e, boolean isHtmlRequest, String forwardUri, String message,
			HttpStatus code)
	{
		if (isHtmlRequest)
		{
			return "forward:" + forwardUri;
		}
		else
		{
			ErrorMessageResponse errorMessageResponse = ErrorMessageResponse.create(message);
			return new ResponseEntity<>(errorMessageResponse, code);
		}
	}

	public static boolean isHtmlRequest(HttpServletRequest httpServletRequest)
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

	public static boolean isHtmlRequest(HandlerMethod handlerMethod)
	{
		return !(handlerMethod.hasMethodAnnotation(ResponseBody.class) || handlerMethod.hasMethodAnnotation(
				ResponseStatus.class) || handlerMethod.getBeanType().isAnnotationPresent(ResponseBody.class)
				|| handlerMethod.getBeanType().isAnnotationPresent(RestController.class));
	}
}
