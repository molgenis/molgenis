package org.molgenis.omx.das.impl;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class DasPatientFilter implements Filter
{
	public void doFilter(ServletRequest servletRequest, ServletResponse response, FilterChain chain) throws IOException,
			ServletException
	{

		HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
		String requestURI = httpServletRequest.getRequestURI();
        int patientIndex = requestURI.indexOf("patient_");
        int datasetIndex = requestURI.indexOf("dataset_");
		if (patientIndex != -1)
		{
			String patientPart = requestURI.substring(patientIndex);
			int slashIndex = patientPart.indexOf("/");
            String patientId = patientPart.substring(0, slashIndex);
            String newDasURI = requestURI.replace(patientId+"/", "");
            String newQueryString = createNewQueryString(httpServletRequest, patientId);
			
			FilteredRequest requestWrapper = new FilteredRequest(servletRequest);
			if(newQueryString != null){
                requestWrapper.setQuery(newQueryString);
            }
			servletRequest.getRequestDispatcher(newDasURI).forward(requestWrapper, response);
		}
        if (datasetIndex != -1)
        {
            String dataSetURLPart = requestURI.substring(datasetIndex);
            int slashIndex = dataSetURLPart.indexOf("/");
            String dataset = dataSetURLPart.substring(0, slashIndex);
            String newDasURI = requestURI.replace(dataset+"/", "");
            String newQueryString = createNewQueryString(httpServletRequest, dataset);

            FilteredRequest requestWrapper = new FilteredRequest(servletRequest);
            if(newQueryString != null){
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
        if(queryString != null){
            String[] queryArray = queryString.split(":");
            newQueryString = queryArray[0]+","+argument+":"+queryArray[1];
        }
		return newQueryString;
	}

	static class FilteredRequest extends HttpServletRequestWrapper
	{
		String query = null;
		
		public void setQuery(String queryString){
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