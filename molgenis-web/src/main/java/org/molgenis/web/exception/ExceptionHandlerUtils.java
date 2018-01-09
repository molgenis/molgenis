package org.molgenis.web.exception;

import org.molgenis.web.ErrorMessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;

class ExceptionHandlerUtils
{
	private static final String VIEW_EXCEPTION = "view-exception";
	private static final String KEY_ERROR_MESSAGE_RESPONSE = "errorMessageResponse";

	private ExceptionHandlerUtils()
	{
	}

	static Object handleException(Exception e, HandlerMethod handlerMethod, HttpStatus httpStatus)
	{
		ErrorMessageResponse errorMessageResponse = ErrorMessageResponse.create(e.getLocalizedMessage());
		if (isHtmlRequest(handlerMethod))
		{
			Map<String, ?> model = of(KEY_ERROR_MESSAGE_RESPONSE, errorMessageResponse);
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
