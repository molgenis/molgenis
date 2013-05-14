package org.molgenis.compute.db.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.molgenis.framework.db.Database;
import org.molgenis.omx.core.MolgenisEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * HandlerInterceptor to secure Spring controllers (are not secured by Molgenis)
 * 
 * @author erwin
 * 
 */
public class SecurityHandlerInterceptor extends HandlerInterceptorAdapter
{
	@Autowired
	private Database database;

	@Autowired
	private Database unauthorizedDatabase;

	private final String className;

	public SecurityHandlerInterceptor(String className)
	{
		this.className = className;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
	{
		if (database.getLogin().canRead(MolgenisEntity.findByClassName(unauthorizedDatabase, className)))
		{
			return true;
		}

		response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		return false;
	}

}
