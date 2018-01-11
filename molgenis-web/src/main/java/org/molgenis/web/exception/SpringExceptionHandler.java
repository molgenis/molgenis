package org.molgenis.web.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import static org.molgenis.web.exception.ExceptionHandlerUtils.handleException;

@ControllerAdvice
@Order(0)//After the Global handler but before the fallback
public class SpringExceptionHandler
{
	private static final Logger LOG = LoggerFactory.getLogger(SpringExceptionHandler.class);

	@SuppressWarnings("deprecation")
	@ExceptionHandler({ NoSuchRequestHandlingMethodException.class, HttpRequestMethodNotSupportedException.class,
			HttpMediaTypeNotSupportedException.class, HttpMediaTypeNotAcceptableException.class,
			MissingPathVariableException.class, MissingServletRequestParameterException.class,
			ServletRequestBindingException.class, ConversionNotSupportedException.class, TypeMismatchException.class,
			HttpMessageNotReadableException.class, HttpMessageNotWritableException.class,
			MethodArgumentNotValidException.class, MissingServletRequestPartException.class, BindException.class,
			NoHandlerFoundException.class, AsyncRequestTimeoutException.class })
	public final Object handleSpringException(Exception ex, @Nullable HandlerMethod handlerMethod, HttpServletRequest httpServletRequest)
	{
		HttpStatus status;
		if (ex instanceof NoSuchRequestHandlingMethodException || ex instanceof NoHandlerFoundException)
		{
			status = HttpStatus.NOT_FOUND;
			return handleException(ex, httpServletRequest, status, null);
		}
		else if (ex instanceof HttpRequestMethodNotSupportedException)
		{
			status = HttpStatus.METHOD_NOT_ALLOWED;
			return handleException(ex, handlerMethod, status, null);
		}
		else if (ex instanceof HttpMediaTypeNotSupportedException)
		{
			status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
			return handleException(ex, handlerMethod, status, null);
		}
		else if (ex instanceof HttpMediaTypeNotAcceptableException)
		{
			status = HttpStatus.NOT_ACCEPTABLE;
			return handleException(ex, handlerMethod, status, null);
		}
		else if (ex instanceof MissingServletRequestParameterException || ex instanceof ServletRequestBindingException
				|| ex instanceof TypeMismatchException || ex instanceof HttpMessageNotReadableException || ex instanceof MethodArgumentNotValidException || ex instanceof MissingServletRequestPartException
				|| ex instanceof BindException)
		{
			status = HttpStatus.BAD_REQUEST;
			return handleException(ex, handlerMethod, status, null);
		}
		else if (ex instanceof MissingPathVariableException || ex instanceof ConversionNotSupportedException || ex instanceof HttpMessageNotWritableException)
		{
			status = HttpStatus.INTERNAL_SERVER_ERROR;
			return handleException(ex, handlerMethod, status, null);
		}
		else if (ex instanceof AsyncRequestTimeoutException)
		{
			status = HttpStatus.SERVICE_UNAVAILABLE;
			return handleException(ex, handlerMethod, status, null);
		}
		else
		{
			if (LOG.isWarnEnabled())
			{
				LOG.warn("Unknown exception type: " + ex.getClass().getName());
			}

			status = HttpStatus.INTERNAL_SERVER_ERROR;
			return handleException(ex, handlerMethod, status, null);
		}
	}
}
