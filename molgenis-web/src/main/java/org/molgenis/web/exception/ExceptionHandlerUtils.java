package org.molgenis.web.exception;

import org.molgenis.web.ErrorMessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

class ExceptionHandlerUtils
{
	private static final String VIEW_EXCEPTION = "view-exception";
	private static final String KEY_ERROR_MESSAGE_RESPONSE = "errorMessageResponse";
	public static final String DEVELOPMENT = "development";
	public static final String PRODUCTION = "production";
	public static final String STACK_TRACE = "stackTrace";
	public static final String HTTP_STATUS_CODE = "httpStatusCode";

	private ExceptionHandlerUtils()
	{
	}

	static Object handleException(Exception e, HandlerMethod handlerMethod, HttpStatus httpStatus, String errorCode, String environment)
	{
		ErrorMessageResponse errorMessageResponse = ErrorMessageResponse.create(e.getLocalizedMessage(), errorCode);
		if (isHtmlRequest(handlerMethod))
		{
			Map<String, Object> model = new HashMap<>();
			model.put(KEY_ERROR_MESSAGE_RESPONSE, errorMessageResponse);
			model.put(HTTP_STATUS_CODE, httpStatus.value());
			if (environment.equals(DEVELOPMENT))
			{
				model.put(STACK_TRACE, e.getStackTrace());
			}
			return new ModelAndView(VIEW_EXCEPTION, model, httpStatus);
		}
		else
		{
			return new ResponseEntity<>(errorMessageResponse, httpStatus);
		}
	}

	private static boolean isHtmlRequest(HandlerMethod handlerMethod)
	{
		return !(handlerMethod.hasMethodAnnotation(ResponseBody.class) || handlerMethod.hasMethodAnnotation(
				ResponseStatus.class) || handlerMethod.getBeanType().isAnnotationPresent(ResponseBody.class)
				|| handlerMethod.getBeanType().isAnnotationPresent(RestController.class));
	}
}
