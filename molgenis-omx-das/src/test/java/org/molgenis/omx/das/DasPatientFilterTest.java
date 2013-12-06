package org.molgenis.omx.das;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mockito.ArgumentCaptor;
import org.molgenis.util.HandleRequestDelegationException;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import uk.ac.ebi.mydas.exceptions.DataSourceException;
import uk.ac.ebi.mydas.exceptions.UnimplementedFeatureException;

public class DasPatientFilterTest
{
	private HttpServletRequest request;
	private HttpServletResponse response;
	private DasPatientFilter filter;
	private FilterChain chain;
	private HttpServletRequest requestNoPatient;
	private HttpServletResponse responseNoPatient;
	private RequestDispatcher requestDispatcher;

	@SuppressWarnings("unchecked")
	@BeforeTest
	public void setUp() throws HandleRequestDelegationException, Exception
	{
		Map<URL, String> linkout = new HashMap<URL, String>();
		linkout.put(new URL("http://www.molgenis.org/"), "Link");

		request = mock(HttpServletRequest.class);
		response = mock(HttpServletResponse.class);
		chain = mock(FilterChain.class);
		requestDispatcher = mock(RequestDispatcher.class);
		filter = new DasPatientFilter();
		when(request.getRequestURI()).thenReturn("/das/col7a1/patient502/features");
		when(request.getQueryString()).thenReturn("3:48618447,48640609;maxbins=636");
		when(request.getRequestDispatcher("/das/col7a1/features")).thenReturn(requestDispatcher);
		requestNoPatient = mock(HttpServletRequest.class);
		responseNoPatient = mock(HttpServletResponse.class);
		when(requestNoPatient.getRequestURI()).thenReturn("/das/col7a1/features");
		when(requestNoPatient.getQueryString()).thenReturn("3:48618447,48640609;maxbins=636");
		when(requestNoPatient.getRequestDispatcher("/das/col7a1/features")).thenReturn(requestDispatcher);
	}

	@Test()
	public void doFilterPatientTest() throws UnimplementedFeatureException, DataSourceException, IOException,
			ServletException
	{
		ArgumentCaptor<HttpServletRequest> argumentRequest = ArgumentCaptor.forClass(HttpServletRequest.class);
		ArgumentCaptor<HttpServletResponse> argumentResponse = ArgumentCaptor.forClass(HttpServletResponse.class);
		filter.doFilter(request, response, chain);
		verify(requestDispatcher).forward(argumentRequest.capture(), argumentResponse.capture());
		assertEquals("3,502:48618447,48640609;maxbins=636", argumentRequest.getValue().getQueryString());
	}

	@Test()
	public void doFilterNoPatientTest() throws UnimplementedFeatureException, DataSourceException, IOException,
			ServletException
	{
		filter.doFilter(requestNoPatient, responseNoPatient, chain);
		verify(chain).doFilter(eq(requestNoPatient), eq(responseNoPatient));
	}
}
