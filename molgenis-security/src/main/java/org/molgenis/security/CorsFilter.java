package org.molgenis.security;

import org.molgenis.security.token.TokenExtractor;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter to enable Cross-origin resource sharing (CORS)
 */
public class CorsFilter extends OncePerRequestFilter
{
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException
	{
		if (request.getHeader("Access-Control-Request-Method") != null && "OPTIONS".equals(request.getMethod()))
		{
			// respond to pre-flight CORS request
			response.addHeader("Access-Control-Allow-Origin", "*");
			response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
			response.addHeader("Access-Control-Allow-Headers", "Content-Type, " + TokenExtractor.TOKEN_HEADER);
			response.addHeader("Access-Control-Max-Age", "1800");
			response.setStatus(HttpServletResponse.SC_OK);
			return;
		}

		filterChain.doFilter(request, response);
	}
}