package org.molgenis.framework.server.async;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisFrontController;

public abstract class AsyncMolgenisFrontController extends MolgenisFrontController implements AsyncMolgenisService
{
	// helper vars
	private static final long serialVersionUID = -2141508157810793106L;
	Logger logger = Logger.getLogger(AsyncMolgenisFrontController.class);

	// map of all services for this app
	protected Map<String, AsyncMolgenisService> services;

	// context
	protected AsyncMolgenisContext context;

	// the one and only service() used in the molgenis app
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response)
	{
		try
		{
			// wrap request and response
			AsyncMolgenisRequest req = new AsyncMolgenisRequest(request, response); // TODO:
																					// Bad,
																					// but
																					// needed
																					// for
																					// redirection.
																					// DISCUSS.
			AsyncMolgenisResponse res = new AsyncMolgenisResponse(response);

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
	public void handleRequest(AsyncMolgenisRequest request, AsyncMolgenisResponse response) throws ParseException,
			DatabaseException, IOException
	{
		long startTime = System.currentTimeMillis();
		HttpServletRequest req = request.getRequest();
		String path = req.getRequestURI().substring(context.getVariant().length() + 1);
		if (path.equals("")) path = "/";

		UUID id = UUID.randomUUID();
		request.setLoadingScreenId(id);
		this.context.getLoadingScreenUUIDFactory().addLoadingId(id, path);
		services.get("/loadingscreen").handleRequest(request, response);

		for (String p : services.keySet())
		{
			if (path.startsWith(p))
			{

				// if mapped to "/", we assume we are serving out a file, and do
				// not manage security/connections
				if (p.equals("/"))
				{
					System.out.println("> serving file: " + path);
					services.get(p).handleRequest(request, response);
				}
				else
				{
					System.out.println("> new request to '" + path + "' handled by "
							+ services.get(p).getClass().getSimpleName() + " mapped on path " + p);
					System.out.println("request content: " + request.toString());

					UUID connId = getSecuredDatabase(request);

					System.out.println("database status: "
							+ (request.getDatabase().getLogin().isAuthenticated() ? "authenticated as "
									+ request.getDatabase().getLogin().getUserName() : "not authenticated"));

					request.setServicePath(p);
					services.get(p).handleAsyncRequest(request, id);
					manageConnection(connId, startTime);

					// printSessionInfo(req.getSession());
					// context.getTokenFactory().printTokens();
				}

				return;
			}

		}
	}

}
