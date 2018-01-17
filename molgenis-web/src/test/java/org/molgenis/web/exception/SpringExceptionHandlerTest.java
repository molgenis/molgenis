package org.molgenis.web.exception;

import com.google.common.collect.ImmutableList;
import org.mockito.Mock;
import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.i18n.properties.AllPropertiesMessageSource;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.web.ErrorMessageResponse;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;
import static org.testng.Assert.assertEquals;

public class SpringExceptionHandlerTest extends AbstractMockitoTest
{
	private SpringExceptionHandler handler;
	private ObjectError globalError;
	private FieldError fieldError;
	@Mock
	private HandlerMethod handlerMethod;
	@Mock
	private HttpServletRequest httpServletRequest;
	@Mock
	private NoHandlerFoundException noHandlerFoundException;
	@Mock
	private AsyncRequestTimeoutException asyncRequestTimeoutException;
	@Mock
	private BindException bindException;
	@Mock
	private MissingServletRequestPartException missingServletRequestPartException;
	@Mock
	private MethodArgumentNotValidException methodArgumentNotValidException;
	@Mock
	private BindingResult bindingResult;
	@Mock
	private HttpMediaTypeNotAcceptableException httpMediaTypeNotAcceptableException;
	@Mock
	private HttpMessageNotWritableException httpMessageNotWritableException;
	@Mock
	private HttpMessageNotReadableException httpMessageNotReadableException;
	@Mock
	private TypeMismatchException typeMismatchException;
	@Mock
	private HttpMediaTypeNotSupportedException httpMediaTypeNotSupportedException;
	@Mock
	private ServletRequestBindingException servletRequestBindingException;
	@Mock
	private ConversionNotSupportedException conversionNotSupportedException;
	@Mock
	private MissingServletRequestParameterException missingServletRequestParameterException;
	@Mock
	private MissingPathVariableException missingPathVariableException;

	@BeforeClass
	public void beforeClass()
	{
		AllPropertiesMessageSource molgenisLocalizationMessages = new AllPropertiesMessageSource();
		molgenisLocalizationMessages.addMolgenisNamespaces("web");
		ResourceBundleMessageSource hibernateValidationMessages = new ResourceBundleMessageSource();
		hibernateValidationMessages.addBasenames("org.hibernate.validator.ValidationMessages");
		molgenisLocalizationMessages.setParentMessageSource(hibernateValidationMessages);
		MessageSourceHolder.setMessageSource(molgenisLocalizationMessages);

		globalError = new ObjectError("entityCollectionRequestV2", new String[] { "TwoFieldsSet" }, new Object[] { 1 },
				"must have two fields set");
		fieldError = new FieldError("entityCollectionRequestV2", "num", -10, false,
				new String[] { "Min.entityCollectionRequestV2.num", "Min.num", "Min.int", "Min" }, new Object[] {
				new DefaultMessageSourceResolvable(new String[] { "entityCollectionRequestV2.num", "num" }, null,
						"num"), 0 }, "must be greater than or equal to 0");
	}

	@BeforeMethod
	public void setUp()
	{
		handler = new SpringExceptionHandler();
	}

	@Test
	public void testNoHandlerFoundException()
	{
		when(noHandlerFoundException.getLocalizedMessage()).thenReturn("localized message");
		when(httpServletRequest.getMethod()).thenReturn("OPTIONS");
		assertEquals(handler.handleSpringException(noHandlerFoundException, httpServletRequest), new ResponseEntity<>(
				new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage("localized message")), NOT_FOUND));
	}

	@Test
	public void AsyncRequestTimeoutException()
	{
		when(handlerMethod.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);
		when(asyncRequestTimeoutException.getLocalizedMessage()).thenReturn("localized message");
		assertEquals(handler.handleSpringException(asyncRequestTimeoutException, handlerMethod), new ResponseEntity<>(
				new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage("localized message")),
				SERVICE_UNAVAILABLE));
	}

	@Test
	public void testBindException()
	{
		when(handlerMethod.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);
		when(bindException.getGlobalErrors()).thenReturn(ImmutableList.of(globalError));
		when(bindException.getFieldErrors()).thenReturn(ImmutableList.of(fieldError));

		assertEquals(handler.handleSpringException(bindException, handlerMethod), new ResponseEntity<>(
				new ErrorMessageResponse(ImmutableList.of(
						new ErrorMessageResponse.ErrorMessage("'entityCollectionRequestV2' must have two fields set",
								"TwoFieldsSet"), new ErrorMessageResponse.ErrorMessage(
								"Field 'num' of 'entityCollectionRequestV2' must be greater than or equal to 0",
								"Min"))), BAD_REQUEST));
	}

	@Test
	public void testMissingServletRequestPartException()
	{
		when(handlerMethod.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);
		when(missingServletRequestPartException.getLocalizedMessage()).thenReturn("localized message");

		assertEquals(handler.handleSpringException(missingServletRequestPartException, handlerMethod),
				new ResponseEntity<>(
						new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage("localized message")),
						BAD_REQUEST));
	}

	@Test
	public void testMethodArgumentNotValidException()
	{
		when(handlerMethod.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);
		when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
		when(bindingResult.getGlobalErrors()).thenReturn(ImmutableList.of(globalError));
		when(bindingResult.getFieldErrors()).thenReturn(ImmutableList.of(fieldError));

		assertEquals(handler.handleSpringException(methodArgumentNotValidException, handlerMethod),
				new ResponseEntity<>(new ErrorMessageResponse(ImmutableList.of(
						new ErrorMessageResponse.ErrorMessage("'entityCollectionRequestV2' must have two fields set",
								"TwoFieldsSet"), new ErrorMessageResponse.ErrorMessage(
								"Field 'num' of 'entityCollectionRequestV2' must be greater than or equal to 0",
								"Min"))), BAD_REQUEST));
	}

	@Test
	public void testHttpMessageNotWritableException()
	{
		when(handlerMethod.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);
		when(httpMessageNotWritableException.getLocalizedMessage()).thenReturn("localized message");

		assertEquals(handler.handleSpringException(httpMessageNotWritableException, handlerMethod),
				new ResponseEntity<>(
						new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage("localized message")),
						INTERNAL_SERVER_ERROR));
	}

	@Test
	public void testHttpMessageNotReadableException()
	{
		when(handlerMethod.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);
		when(httpMessageNotReadableException.getLocalizedMessage()).thenReturn("localized message");

		assertEquals(handler.handleSpringException(httpMessageNotReadableException, handlerMethod),
				new ResponseEntity<>(
						new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage("localized message")),
						BAD_REQUEST));
	}

	@Test
	public void testTypeMismatchException()
	{
		when(handlerMethod.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);
		when(typeMismatchException.getLocalizedMessage()).thenReturn("localized message");

		assertEquals(handler.handleSpringException(typeMismatchException, handlerMethod), new ResponseEntity<>(
				new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage("localized message")), BAD_REQUEST));
	}

	@Test
	public void testConversionNotSupportedException()
	{
		when(handlerMethod.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);
		when(conversionNotSupportedException.getLocalizedMessage()).thenReturn("localized message");

		assertEquals(handler.handleSpringException(conversionNotSupportedException, handlerMethod),
				new ResponseEntity<>(
						new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage("localized message")),
						INTERNAL_SERVER_ERROR));
	}

	@Test
	public void testServletRequestBindingException()
	{
		when(handlerMethod.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);
		when(servletRequestBindingException.getLocalizedMessage()).thenReturn("localized message");

		assertEquals(handler.handleSpringException(servletRequestBindingException, handlerMethod), new ResponseEntity<>(
				new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage("localized message")), BAD_REQUEST));
	}

	@Test
	public void testMissingServletRequestParameterExceptionn()
	{
		when(handlerMethod.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);
		when(missingServletRequestParameterException.getLocalizedMessage()).thenReturn("localized message");

		assertEquals(handler.handleSpringException(missingServletRequestParameterException, handlerMethod),
				new ResponseEntity<>(
						new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage("localized message")),
						BAD_REQUEST));
	}

	@Test
	public void testMissingPathVariableException()
	{
		when(handlerMethod.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);
		when(missingPathVariableException.getLocalizedMessage()).thenReturn("localized message");

		assertEquals(handler.handleSpringException(missingPathVariableException, handlerMethod), new ResponseEntity<>(
				new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage("localized message")),
				INTERNAL_SERVER_ERROR));
	}

	@Test
	public void testHttpMediaTypeNotAcceptableException()
	{
		when(handlerMethod.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);
		when(httpMediaTypeNotAcceptableException.getLocalizedMessage()).thenReturn("localized message");

		assertEquals(handler.handleSpringException(httpMediaTypeNotAcceptableException, handlerMethod),
				new ResponseEntity<>(
						new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage("localized message")),
						NOT_ACCEPTABLE));
	}

	@Test
	public void testHttpMediaTypeNotSupportedException()
	{
		when(handlerMethod.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);
		when(httpMediaTypeNotSupportedException.getLocalizedMessage()).thenReturn("localized message");

		assertEquals(handler.handleSpringException(httpMediaTypeNotSupportedException, handlerMethod),
				new ResponseEntity<>(
						new ErrorMessageResponse(new ErrorMessageResponse.ErrorMessage("localized message")),
						UNSUPPORTED_MEDIA_TYPE));
	}
}