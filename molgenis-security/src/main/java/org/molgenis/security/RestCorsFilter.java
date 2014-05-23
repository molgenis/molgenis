package org.molgenis.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.web.filter.GenericFilterBean;

public class RestCorsFilter extends GenericFilterBean
{

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException
	{
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		if (httpRequest.getRequestURI().startsWith("/api/"))
		{
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			if (httpRequest.getMethod() == HttpMethod.OPTIONS.toString())
			{
				// preflight CORS request
				httpResponse.setHeader("Access-Control-Allow-Origin", "*");
				httpResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE");
				httpResponse.setHeader("Access-Control-Allow-Headers", "Accept, Content-Type, Origin");
			}
			else
			{
				// actual CORS request
				httpResponse.setHeader("Access-Control-Allow-Origin", "*");
				httpResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE");
				httpResponse.setHeader("Access-Control-Allow-Headers", "x-requested-with,x-molgenis-token");
			}
		}
		chain.doFilter(request, response);
	}
}
