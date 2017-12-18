package org.molgenis.test;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.util.NestedServletException;

import static org.testng.Assert.fail;

/**
 * Exceptions that get thrown when MockMvc is used are sometimes wrapped in a NestedServletException. This helper class unwraps
 * and rethrows the original exception so it can be tested with the 'expectedExceptions' parameter of the @Test annotation.
 * It also provides a way to add some verification to the test after the exception has been thrown.
 */
public class MockMvcExceptionalRequestPerformer
{
	private final MockMvc mockMvc;

	public MockMvcExceptionalRequestPerformer(MockMvc mockMvc)
	{
		this.mockMvc = mockMvc;
	}

	@SuppressWarnings("squid:S00108") // The empty code block is part of a lambda and should be empty
	public void perform(MockHttpServletRequestBuilder request) throws Throwable
	{
		perform(request, () ->
		{
		});
	}

	@SuppressWarnings("squid:S00112") // Using the generic Throwable is exactly what we want to do here
	public void perform(MockHttpServletRequestBuilder request, Runnable verification) throws Throwable
	{
		MvcResult result;
		try
		{
			result = mockMvc.perform(request).andReturn();
		}
		catch (NestedServletException nestedServletException)
		{
			throw nestedServletException.getCause();
		}
		finally
		{
			verification.run();
		}

		throwUnwrappedException(result);
	}

	@SuppressWarnings("squid:S00112") // Using a generic Exception is exactly what we want to do here
	private void throwUnwrappedException(MvcResult result) throws Exception
	{
		if (result.getResolvedException() != null)
		{
			throw result.getResolvedException();
		}
		else
		{
			fail("Request should've thrown an exception");
		}
	}
}
