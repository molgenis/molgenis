package org.molgenis.framework.server;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.RollingFileAppender;
import org.molgenis.MolgenisOptions;
import org.molgenis.framework.db.DatabaseException;

public abstract class MolgenisFrontController extends HttpServlet implements MolgenisService
{
	// helper vars
	private static final long serialVersionUID = -2141508157810793106L;
	protected Logger logger;
	private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss 'on' dd MMMM yyyy");

	// map of all services for this app
	protected Map<String, MolgenisService> services;

	// the used molgenisoptions, set by generated MolgenisServlet
	protected MolgenisOptions usedOptions = null;

	// context
	protected MolgenisContext context;

	// the one and only service() used in the molgenis app
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response)
	{
		try
		{

			@SuppressWarnings("rawtypes")
			Enumeration attributeNames = request.getAttributeNames();
			while (attributeNames.hasMoreElements())
			{
				String nextElement = (String) attributeNames.nextElement();
				System.out.println(String.format("---> %s: %s", nextElement, request.getAttribute(nextElement)));
			}

			// wrap request and response
			MolgenisRequest req = new MolgenisRequest(request, response);
			// req.setDatabase(DatabaseUtil.getDatabase());

			// TODO: Bad, but needed for redirection. DISCUSS.
			MolgenisResponse res = new MolgenisResponse(response);

			// handle the request with current database + login
			this.handleRequest(req, res);
		}
		catch (Exception e)
		{
			// TODO: send generic error page with details
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void handleRequest(MolgenisRequest request, MolgenisResponse response) throws ParseException,
			DatabaseException, IOException
	{
		HttpServletRequest req = request.getRequest();
		if (usedOptions.block_webspiders)
		{
			// block spiders (webcrawlers) if the option has been set (default
			// is false)
			String userAgent = req.getHeader("User-Agent");
			for (String spider : new String[]
			{ "Googlebot", "Yammybot", "Openbot", "Yahoo", "Slurp", "msnbot", "ia_archiver", "Lycos", "Scooter",
					"AltaVista", "Teoma", "Gigabot", "Googlebot-Mobile" })
			{
				if (userAgent != null && userAgent.contains(spider))
				{
					response.response.sendError(403, "This page is forbidden for spiders.");
					return;
				}
			}
		}

		String servletPath = req.getServletPath();
		String baseURL = req.getRequestURL().substring(0, req.getRequestURL().length() - servletPath.length());
		for (String servicePath : services.keySet())
		{
			System.out.println("servicePath for service=" + servicePath);
			if (servletPath.startsWith(servicePath))
			{
				Date date = new Date();

				// if mapped to "/", we assume we are serving out a file, and do
				// not manage security/connections
				if (servicePath.equals("/"))
				{
					System.out.println("> serving file: " + servletPath);
					services.get(servicePath).handleRequest(request, response);
				}
				else
				{
					System.out.println("> new request \"" + servletPath + "\" from "
							+ request.getRequest().getRemoteHost() + " at " + dateFormat.format(date) + " handled by "
							+ services.get(servicePath).getClass().getSimpleName() + " mapped on path " + servicePath);
					System.out.println("request fields: " + request.toString());

					System.out.println("database status: "
							+ (request.getDatabase().getLogin().isAuthenticated() ? "authenticated as "
									+ request.getDatabase().getLogin().getUserName() : "not authenticated"));

					// e.g. "http://localhost:8080/xqtl"
					request.setAppLocation(baseURL);

					// e.g. "/api/R/"
					request.setServicePath(servicePath);

					// e.g. "/api/R/source.R"
					request.setRequestPath(servletPath);

					services.get(servicePath).handleRequest(request, response);

				}

				return;
			}
		}
	}

	protected void createLogger() throws ServletException
	{
		try
		{
			if (StringUtils.isEmpty(usedOptions.log4j_properties_uri))
			{
				// get logger and remove appenders added by classpath JARs. (=
				// evil)
				Logger rootLogger = Logger.getRootLogger();
				rootLogger.removeAllAppenders();

				// the pattern used to format the logger output
				PatternLayout pattern = new PatternLayout("%-4r %-5p [%c] %m%n");

				// get the level from the molgenis options
				rootLogger.setLevel(usedOptions.log_level);

				// console appender
				if (usedOptions.log_target.equals(MolgenisOptions.LogTarget.CONSOLE))
				{
					rootLogger.addAppender(new ConsoleAppender(pattern));
					System.out.println("Log4j CONSOLE appender added log level " + usedOptions.log_level);
				}

				// file appender
				if (usedOptions.log_target.equals(MolgenisOptions.LogTarget.FILE))
				{
					RollingFileAppender fa = new RollingFileAppender(pattern, "logger.out");
					fa.setMaximumFileSize(100000000); // 100MB
					rootLogger.addAppender(fa);
					System.out.println("Log4j FILE appender added with level " + usedOptions.log_level
							+ ", writing to: " + new File(fa.getFile()).getAbsolutePath());
				}

				// add no appender at all
				if (usedOptions.log_target.equals(MolgenisOptions.LogTarget.OFF))
				{
					System.out.println("Log4j logger turned off");
				}
			}
			else
			{
				ClassLoader loader = this.getClass().getClassLoader();
				URL urlLog4jProp = loader.getResource(usedOptions.log4j_properties_uri);
				if (urlLog4jProp == null)
				{
					System.out
							.println(String
									.format("*** Incorrect log4j_properties_uri : '%s' in Molgenis properties file, so initializing log4j with BasicConfigurator",
											urlLog4jProp));
					BasicConfigurator.configure();
				}
				else
				{
					System.out.println(String.format("*** Log4j initializing with config file %s", urlLog4jProp));
					PropertyConfigurator.configure(urlLog4jProp);
				}
			}
		}
		catch (Exception e)
		{
			throw new ServletException(e);
		}
	}
}
