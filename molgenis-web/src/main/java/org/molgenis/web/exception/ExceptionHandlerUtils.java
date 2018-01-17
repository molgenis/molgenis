package org.molgenis.web.exception;

import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.web.ErrorMessageResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.util.matcher.ELRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class ExceptionHandlerUtils
{
	private static final String VIEW_EXCEPTION = "view-exception";
	private static final String KEY_ERROR_MESSAGE_RESPONSE = "errorMessageResponse";
	public static final String DEVELOPMENT = "development";
	public static final String PRODUCTION = "production";
	public static final String STACK_TRACE = "stackTrace";
	public static final String HTTP_STATUS_CODE = "httpStatusCode";

	private static final RequestMatcher requestMatcher = new ELRequestMatcher(
			"hasHeader('X-Requested-With','XMLHttpRequest')");
	public static final String OPTIONS = "OPTIONS";

	private ExceptionHandlerUtils()
	{
	}

	static Object handleException(Exception e, HandlerMethod handlerMethod, HttpStatus httpStatus, String errorCode,
			String environment)
	{
		return handleException(e, isHtmlRequest(handlerMethod), httpStatus, errorCode, environment);
	}

	static Object handleException(Exception e, HandlerMethod handlerMethod, HttpStatus httpStatus, String errorCode)
	{
		return handleException(e, isHtmlRequest(handlerMethod), httpStatus, errorCode, PRODUCTION);
	}

	static Object handleException(Exception e, HttpServletRequest httpServletRequest, HttpStatus httpStatus,
			String errorCode, String environment)
	{
		return handleException(e, isHtmlRequest(httpServletRequest), httpStatus, errorCode, environment);
	}

	static Object handleException(Exception e, HttpServletRequest httpServletRequest, HttpStatus httpStatus,
			String errorCode)
	{
		return handleException(e, isHtmlRequest(httpServletRequest), httpStatus, errorCode, PRODUCTION);
	}

	private static Object handleException(Exception e, boolean isHtmlRequest, HttpStatus httpStatus, String errorCode,
			String environment)
	{
		ErrorMessageResponse errorMessageResponse = createErrorMessageResponse(e, errorCode);
		if (isHtmlRequest)
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

	private static ErrorMessageResponse createErrorMessageResponse(Exception e, String errorCode)
	{
		Locale locale = LocaleContextHolder.getLocale();
		ErrorMessageResponse result = new ErrorMessageResponse();
		if (e instanceof MethodArgumentNotValidException)
		{
			MethodArgumentNotValidException methodArgumentNotValidException = (MethodArgumentNotValidException) e;
			BindingResult bindingResult = methodArgumentNotValidException.getBindingResult();
			MessageSource messageSource = MessageSourceHolder.getMessageSource();

			for (ObjectError objectError : bindingResult.getGlobalErrors())
			{
				String message = messageSource.getMessage(objectError, locale);
				result.addErrorMessage(new ErrorMessageResponse.ErrorMessage(message, objectError.getCode()));
			}
			for (FieldError fieldError : bindingResult.getFieldErrors())
			{
				String message = messageSource.getMessage(fieldError, locale);
				result.addErrorMessage(new ErrorMessageResponse.ErrorMessage(message, fieldError.getCode()));
			}

		}
		else
		{
			result.addErrorMessage(new ErrorMessageResponse.ErrorMessage(e.getLocalizedMessage(), errorCode));
		}
		return result;
	}

	private static boolean isHtmlRequest(HandlerMethod handlerMethod)
	{
		return !(handlerMethod.hasMethodAnnotation(ResponseBody.class) || handlerMethod.hasMethodAnnotation(
				ResponseStatus.class) || handlerMethod.getBeanType().isAnnotationPresent(ResponseBody.class)
				|| handlerMethod.getBeanType().isAnnotationPresent(RestController.class));
	}

	private static boolean isHtmlRequest(HttpServletRequest httpServletRequest)
	{
		return !(OPTIONS.equals(httpServletRequest.getMethod()) || requestMatcher.matches(httpServletRequest));
	}
}
