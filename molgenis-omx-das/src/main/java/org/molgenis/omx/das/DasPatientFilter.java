package org.molgenis.omx.das;

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
		int patientIndex = requestURI.indexOf("patient");
		if (patientIndex != -1)
		{
			String patientPart = requestURI.substring(patientIndex);
			int slashIndex = patientPart.indexOf("/");			
			String newDasURI = createNewRequestURI(requestURI, patientPart, slashIndex);	
			String newQueryString = createNewQueryString(httpServletRequest, patientPart, slashIndex);
			
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

	private String createNewRequestURI(String requestURI, String patientPart, int slashIndex)
	{
		String URIPatientPart = patientPart.substring(0, slashIndex + 1);
		String newDasURI = requestURI.replace(URIPatientPart, "");
		return newDasURI;
	}

	private String createNewQueryString(HttpServletRequest req, String patientPart, int slashIndex)
	{
        String newQueryString = null;
        String queryString = req.getQueryString();
        if(queryString != null){
            String patientId = patientPart.substring(7, slashIndex);
            String[] queryArray = queryString.split(":");
            newQueryString = queryArray[0]+","+patientId+":"+queryArray[1];
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