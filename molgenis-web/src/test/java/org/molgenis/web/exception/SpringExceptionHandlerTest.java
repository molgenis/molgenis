package org.molgenis.web.exception;

import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.web.ErrorMessageResponse;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class SpringExceptionHandlerTest extends AbstractMockitoTest
{
	private SpringExceptionHandler handler;

	@BeforeMethod
	public void setUp()
	{
		handler = new SpringExceptionHandler();

	}

	@Test
	public void testNoHandlerFoundException()
	{
		HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);

		ResponseEntity expected = new ResponseEntity(
				new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage("localized message")),
				HttpStatus.NOT_FOUND);
		NoHandlerFoundException exception = mock(NoHandlerFoundException.class);
		when(exception.getLocalizedMessage()).thenReturn("localized message");

		when(httpServletRequest.getMethod()).thenReturn("OPTIONS");
		assertEquals(handler.handleSpringException(exception, httpServletRequest), expected);
	}

	@Test
	public void AsyncRequestTimeoutException()
	{
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);

		ResponseEntity expected = new ResponseEntity(
				new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage("localized message")),
				HttpStatus.SERVICE_UNAVAILABLE);
		AsyncRequestTimeoutException exception = mock(AsyncRequestTimeoutException.class);
		when(exception.getLocalizedMessage()).thenReturn("localized message");

		assertEquals(handler.handleSpringException(exception, handlerMethod), expected);
	}

	@Test
	public void testBindException()
	{
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);

		ResponseEntity expected = new ResponseEntity(
				new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage("localized message")),
				HttpStatus.BAD_REQUEST);
		BindException exception = mock(BindException.class);
		when(exception.getLocalizedMessage()).thenReturn("localized message");

		assertEquals(handler.handleSpringException(exception, handlerMethod), expected);
	}

	@Test
	public void testMissingServletRequestPartException()
	{
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);

		ResponseEntity expected = new ResponseEntity(
				new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage("localized message")),
				HttpStatus.BAD_REQUEST);
		MissingServletRequestPartException exception = mock(MissingServletRequestPartException.class);
		when(exception.getLocalizedMessage()).thenReturn("localized message");

		assertEquals(handler.handleSpringException(exception, handlerMethod), expected);
	}

	@Test
	public void testMethodArgumentNotValidException()
	{
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);

		ResponseEntity expected = new ResponseEntity(
				new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage("localized message")),
				HttpStatus.BAD_REQUEST);
		MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
		when(exception.getLocalizedMessage()).thenReturn("localized message");

		assertEquals(handler.handleSpringException(exception, handlerMethod), expected);
	}

	@Test
	public void testHttpMessageNotWritableException()
	{
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);

		ResponseEntity expected = new ResponseEntity(
				new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage("localized message")),
				HttpStatus.INTERNAL_SERVER_ERROR);
		HttpMessageNotWritableException exception = mock(HttpMessageNotWritableException.class);
		when(exception.getLocalizedMessage()).thenReturn("localized message");

		assertEquals(handler.handleSpringException(exception, handlerMethod), expected);
	}

	@Test
	public void testHttpMessageNotReadableException()
	{
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);

		ResponseEntity expected = new ResponseEntity(
				new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage("localized message")),
				HttpStatus.BAD_REQUEST);
		HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);
		when(exception.getLocalizedMessage()).thenReturn("localized message");

		assertEquals(handler.handleSpringException(exception, handlerMethod), expected);
	}

	@Test
	public void testTypeMismatchException()
	{
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);

		ResponseEntity expected = new ResponseEntity(
				new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage("localized message")),
				HttpStatus.BAD_REQUEST);
		TypeMismatchException exception = mock(TypeMismatchException.class);
		when(exception.getLocalizedMessage()).thenReturn("localized message");

		assertEquals(handler.handleSpringException(exception, handlerMethod), expected);
	}

	@Test
	public void testConversionNotSupportedException()
	{
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);

		ResponseEntity expected = new ResponseEntity(
				new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage("localized message")),
				HttpStatus.INTERNAL_SERVER_ERROR);
		ConversionNotSupportedException exception = mock(ConversionNotSupportedException.class);
		when(exception.getLocalizedMessage()).thenReturn("localized message");

		assertEquals(handler.handleSpringException(exception, handlerMethod), expected);
	}

	@Test
	public void testServletRequestBindingException()
	{
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);

		ResponseEntity expected = new ResponseEntity(
				new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage("localized message")),
				HttpStatus.BAD_REQUEST);
		ServletRequestBindingException exception = mock(ServletRequestBindingException.class);
		when(exception.getLocalizedMessage()).thenReturn("localized message");

		assertEquals(handler.handleSpringException(exception, handlerMethod), expected);
	}

	@Test
	public void testMissingServletRequestParameterExceptionn()
	{
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);

		ResponseEntity expected = new ResponseEntity(
				new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage("localized message")),
				HttpStatus.BAD_REQUEST);
		MissingServletRequestParameterException exception = mock(MissingServletRequestParameterException.class);
		when(exception.getLocalizedMessage()).thenReturn("localized message");

		assertEquals(handler.handleSpringException(exception, handlerMethod), expected);
	}

	@Test
	public void testMissingPathVariableException()
	{
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);

		ResponseEntity expected = new ResponseEntity(
				new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage("localized message")),
				HttpStatus.INTERNAL_SERVER_ERROR);
		MissingPathVariableException exception = mock(MissingPathVariableException.class);
		when(exception.getLocalizedMessage()).thenReturn("localized message");

		assertEquals(handler.handleSpringException(exception, handlerMethod), expected);
	}

	@Test
	public void testHttpMediaTypeNotAcceptableException()
	{
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);

		ResponseEntity expected = new ResponseEntity(
				new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage("localized message")),
				HttpStatus.NOT_ACCEPTABLE);
		HttpMediaTypeNotAcceptableException exception = mock(HttpMediaTypeNotAcceptableException.class);
		when(exception.getLocalizedMessage()).thenReturn("localized message");

		assertEquals(handler.handleSpringException(exception, handlerMethod), expected);
	}

	@Test
	public void testHttpMediaTypeNotSupportedException()
	{
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);

		ResponseEntity expected = new ResponseEntity(
				new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage("localized message")),
				HttpStatus.UNSUPPORTED_MEDIA_TYPE);
		HttpMediaTypeNotSupportedException exception = mock(HttpMediaTypeNotSupportedException.class);
		when(exception.getLocalizedMessage()).thenReturn("localized message");

		assertEquals(handler.handleSpringException(exception, handlerMethod), expected);
	}
}