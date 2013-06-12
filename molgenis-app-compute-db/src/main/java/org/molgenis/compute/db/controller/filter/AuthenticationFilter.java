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
                    Map<String, String[]> parameters = request.getParameterMap();
                    String pilotID = parameters.get(PILOT_ID)[0];

                    List< Pilot > pilotList = db.query(Pilot.class).eq(Pilot.VALUE, pilotID)
                            .and().eq(Pilot.STATUS, SUBMITTED).find();
                    if(pilotList.size() == 0)
                    {
                        ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                }
			}
			catch (Exception e)
			{
				throw new ServletException(e);
			}
		}

        System.out.println("parameters");
        Enumeration<String> e = request.getParameterNames();

        while(e.hasMoreElements())
        {
            System.out.println(e.nextElement());
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

}
