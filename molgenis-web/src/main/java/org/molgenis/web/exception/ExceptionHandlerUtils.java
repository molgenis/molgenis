package org.molgenis.web.exception;

import org.molgenis.web.ErrorMessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.util.matcher.ELRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

public class ExceptionHandlerUtils
{
	private static final RequestMatcher requestMatcher = new ELRequestMatcher(
			"hasHeader('X-Requested-With','XMLHttpRequest')");
	public static final String OPTIONS = "OPTIONS";

	public static Object handleTypedException(boolean isHtmlRequest, String forwardUri, String message, HttpStatus code)
	{
		return handleTypedException(isHtmlRequest, forwardUri, message, code, null);
	}

	public static Object handleTypedException(boolean isHtmlRequest, String forwardUri, String message, HttpStatus code,
			@Nullable String errorCode)
	{
		if (isHtmlRequest)
		{
			return "forward:" + forwardUri;
		}
		else
		{
			ErrorMessageResponse errorMessageResponse = ErrorMessageResponse.create(message, errorCode);
			return new ResponseEntity<>(errorMessageResponse, code);
		}
	}

	public static boolean isHtmlRequest(HttpServletRequest httpServletRequest)
	{
		return !(OPTIONS.equals(httpServletRequest.getMethod()) || requestMatcher.matches(httpServletRequest));
	}

	public static boolean isHtmlRequest(HandlerMethod handlerMethod)
	{
		return !(handlerMethod.hasMethodAnnotation(ResponseBody.class) || handlerMethod.hasMethodAnnotation(
				ResponseStatus.class) || handlerMethod.getBeanType().isAnnotationPresent(ResponseBody.class)
				|| handlerMethod.getBeanType().isAnnotationPresent(RestController.class));
	}
}
