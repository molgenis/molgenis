package org.molgenis.web.exception;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.molgenis.web.ErrorMessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.testng.annotations.Test;

import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.web.exception.ExceptionHandlerUtils.handleException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ExceptionHandlerUtilsTest
{
	@SuppressWarnings("unchecked")
	@Test
	public void testHandleExceptionHtmlRequest()
	{
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		Class beanType = ExceptionHandlerUtils.class;
		when(handlerMethod.getBeanType()).thenReturn(beanType);
		HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

		Map<String, ?> expectedModel = of("errorMessageResponse", ErrorMessageResponse.create("message"));
		ModelAndView expectedModelAndView = new ModelAndView("view-exception", expectedModel, httpStatus);
		Object modelAndView = handleException(new Exception("message"), handlerMethod, httpStatus);
		assertTrue(EqualsBuilder.reflectionEquals(modelAndView, expectedModelAndView));
	}

	@Test
	public void testHandleExceptionNonHtmlRequest()
	{
		HandlerMethod handlerMethod = mock(HandlerMethod.class);
		when(handlerMethod.hasMethodAnnotation(ResponseBody.class)).thenReturn(true);
		HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

		assertEquals(handleException(new Exception("message"), handlerMethod, httpStatus),
				new ResponseEntity<>(ErrorMessageResponse.create("message"), httpStatus));
	}
}