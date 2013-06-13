package org.molgenis.compute.db.controller.filter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.molgenis.compute.db.util.BasicAuthentication;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.compute.runtime.Pilot;
import org.molgenis.framework.db.Database;
import org.molgenis.util.ApplicationUtil;

/**
 * Login filter for api classes.
 * <p/>
 * Api client can login by providing a basic authentication header.
 *
 * @author erwin
 */
public class AuthenticationFilter implements Filter {

    public static final String PILOT_ID = "pilotid";
    public static final String SUBMITTED = "submitted";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
		BasicAuthentication.Result auth = BasicAuthentication.getUsernamePassword((HttpServletRequest) request);

		if (auth != null)
		{
			Database db = ApplicationUtil.getDatabase();
			try
			{
				boolean login = db.getLogin().login(db, auth.getUsername(), auth.getPassword());
				if (!login)
				{
					((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
					return;
				}
                else
                {

                }
			}
			catch (Exception e)
			{
				throw new ServletException(e);
			}
		}

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

}
