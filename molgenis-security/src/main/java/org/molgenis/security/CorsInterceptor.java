package org.molgenis.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class CorsInterceptor extends HandlerInterceptorAdapter
{
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
	{
		response.addHeader("Access-Control-Allow-Origin", "*");
		return true;
	}
}