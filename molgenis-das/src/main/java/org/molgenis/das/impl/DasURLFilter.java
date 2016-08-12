package org.molgenis.das.impl;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

public class DasURLFilter implements Filter
{
	public static final String DATASET_PREFIX = "dasdataset_";

	public void doFilter(ServletRequest servletRequest, ServletResponse response, FilterChain chain)
			throws IOException, ServletException
	{

		HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
		String requestURI = httpServletRequest.getRequestURI();
		int datasetIndex = requestURI.indexOf(DATASET_PREFIX);
		if (datasetIndex != -1)
		{
			String dataSetURLPart = requestURI.substring(datasetIndex);
			int slashIndex = dataSetURLPart.indexOf("/");
			String dataset = dataSetURLPart.substring(0, slashIndex);
			String newDasURI = requestURI.replace(dataset + "/", "");
			String newQueryString = createNewQueryString(httpServletRequest, dataset);

			FilteredRequest requestWrapper = new FilteredRequest(servletRequest);
			if (newQueryString != null)
			{
				requestWrapper.setQuery(newQueryString);
			}
			servletRequest.getRequestDispatcher(newDasURI).forward(requestWrapper, response);
		}
		else
		{
			chain.doFilter(servletRequest, response);
		}
	}

	private String createNewQueryString(HttpServletRequest req, String argument)
	{
		String newQueryString = null;
		String queryString = req.getQueryString();
		if (queryString != null)
		{
			String[] queryArray = queryString.split(":");
			newQueryString = queryArray[0] + "," + argument + ":" + queryArray[1];
		}
		return newQueryString;
	}

	static class FilteredRequest extends HttpServletRequestWrapper
	{
		String query = null;

		public void setQuery(String queryString)
		{
			query = queryString;
		}

		public FilteredRequest(ServletRequest request)
		{
			super((HttpServletRequest) request);
		}

		@Override
		public String getQueryString()
		{
			return query;
		}

	}

	public void init(FilterConfig config) throws ServletException
	{
	}

	public void destroy()
	{
	}

}