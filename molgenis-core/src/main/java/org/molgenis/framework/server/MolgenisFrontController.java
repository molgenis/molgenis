package org.molgenis.framework.server;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
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
	private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss 'on' dd MMMM yyyy");

	// map of all services for this app
	protected Map<String, MolgenisService> services;

	// list of all connections
	protected ConcurrentHashMap<UUID, Connection> connections;

	// the used molgenisoptions, set by generated MolgenisServlet
	protected MolgenisOptions usedOptions = null;

	// context
	protected MolgenisContext context;

	// the database given 1 connection per request (setup stored in session,
	// connectionless after request)
	// return a UUID of the connection that was given to this database and was
	// stored in Map<UUID, Connection> connections
	public abstract UUID createDatabase(MolgenisRequest request) throws DatabaseException, SQLException;

	// the datasource to be put in the context
	public abstract DataSource createDataSource();

	// get login from session and set it to database, or create new login
	public abstract void createLogin(MolgenisRequest request) throws Exception;

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

		// lots of info about request variables & webservers @
		// http://gbic.target.rug.nl/forum/showthread.php?tid=690

		// same for every tested webserver: e.g.
		// "http://localhost:8080/xqtl/api/R"
		String requestURL = req.getRequestURL().toString();

		// same for every tested webserver: e.g. "/xqtl/api/R"
		String requestURI = req.getRequestURI();

		// empty for Apache Tomcat, but e.g. "/xqtl" for standalone
		// FIXME workaround to get http://localhost:8080/molgenis.do (without app name) working with Jetty
		String appName = req.getServletPath();
		if (appName.equals("") || req.getClass().getName().equals("org.mortbay.jetty.Request") || req.getClass().getName().equals("org.eclipse.jetty.server.Request"))
		{
			// empty for standalone, but e.g. "/xqtl" for Apache Tomcat
			appName = req.getContextPath();
		}
		
		// turns "http://localhost:8080/xqtl/api/R" into
		// "http://localhost:8080/xqtl"
		String appLocation = requestURL.substring(0, requestURL.length() - (requestURI.length() - appName.length()));

		// turns "http://localhost:8080/xqtl/api/R" into "/api/R"
		String requestPath = requestURL.substring(requestURL.length() - (requestURI.length() - appName.length()));

		for (String servicePath : services.keySet())
		{
			if (requestPath.startsWith(servicePath))
			{
				long startTime = System.currentTimeMillis();
				Date date = new Date();

				// if mapped to "/", we assume we are serving out a file, and do
				// not manage security/connections
				if (servicePath.equals("/"))
				{
					System.out.println("> serving file: " + requestPath);
					services.get(servicePath).handleRequest(request, response);
				}
				else
				{
					System.out.println("> new request \"" + requestPath + "\" from "
							+ request.getRequest().getRemoteHost() + " at " + dateFormat.format(date) + " handled by "
							+ services.get(servicePath).getClass().getSimpleName() + " mapped on path " + servicePath);
					System.out.println("request fields: " + request.toString());

					UUID connId = getSecuredDatabase(request);

					System.out.println("database status: "
							+ (request.getDatabase().getLogin().isAuthenticated() ? "authenticated as "
									+ request.getDatabase().getLogin().getUserName() : "not authenticated"));

					// e.g. "http://localhost:8080/xqtl"
					request.setAppLocation(appLocation);

					// e.g. "/api/R/"
					request.setServicePath(servicePath);

					// e.g. "/api/R/source.R"
					request.setRequestPath(requestPath);

					try
					{
						services.get(servicePath).handleRequest(request, response);
					}
					finally
					{
						manageConnection(connId, startTime);
					}
				}

				return;
			}
		}
	}

	protected UUID getSecuredDatabase(MolgenisRequest req) throws DatabaseException
	{
		try
		{
			// create database, add a single connection from the pool and set in
			// request for use
			UUID connId = this.createDatabase(req);

			// setup login credentials, or reuse from session and apply to
			// database
			this.createLogin(req);

			// return connection id
			return connId;
		}
		catch (Exception e)
		{
			throw new DatabaseException(e);
		}
	}

	protected void manageConnection(UUID connId, long startTime) throws DatabaseException
	{
		if (connections.containsKey(connId))
		{
			try
			{
				// close the connection and check if it really was closed
				connections.get(connId).close();
				if (!connections.get(connId).isClosed())
				{
					throw new DatabaseException("ERROR: connection was not closed!");
				}
			}
			catch (SQLException sqle)
			{
				throw new DatabaseException(sqle);
			}

			// remove from list (does not happen if Exception was thrown)
			connections.remove(connId);

			System.out.println("< request was handled in " + (System.currentTimeMillis() - startTime)
					+ "ms , active database connections: " + connections.size());
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

	// private void printSessionInfo(HttpSession session)
	// {
	// Date created = new Date(session.getCreationTime());
	// Date accessed = new Date(session.getLastAccessedTime());
	// System.out.println("SESSION ID " + session.getId());
	// //System.out.println("SESSION Created: " + created);
	// //System.out.println("SESSION Last Accessed: " + accessed);
	//
	// // print session contents
	//
	// Enumeration e = session.getAttributeNames();
	// while (e.hasMoreElements())
	// {
	// String name = (String) e.nextElement();
	// String value = session.getAttribute(name).toString();
	// System.out.println("SESSION_ATTRIB " + name + " = " + value);
	// }
	// }

	// if (path != null && path.contains("/api/find"))
	// {
	// this.handleDownload(request, response);
	// }
	// else if (path != null && path.contains("/api/add"))
	// {
	// this.handleUpload(request, response);
	// }
	// else if (path != null && path.contains("/api/R"))
	// {
	// this.handleRAPIrequest(request, response);
	// }
	// else if (path != null && (path.contains("/api/soap")))
	// {
	// this.handleSOAPrequest(request, response);
	// }
	// else if (path != null && path.contains("/xref/find"))
	// {
	// this.handleXREFrequest(request, response);
	// }
	// else if (path != null && path.contains("/download/"))
	// {
	// this.handleDownloadFile(request, response);
	// }

}
