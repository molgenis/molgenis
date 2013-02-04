package org.molgenis.framework.server.async;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.async.LoadingScreenFactory.LoadingScreen;

public class MolgenisLoadingScreenService implements AsyncMolgenisService
{
	Logger logger = Logger.getLogger(MolgenisLoadingScreenService.class);

	private AsyncMolgenisContext mc;

	public MolgenisLoadingScreenService(AsyncMolgenisContext mc)
	{
		this.mc = mc;
	}

	/**
	 * Handle use of the loading screen
	 * 
	 * 
	 * @param request
	 * @param response
	 */
	@Override
	public void handleRequest(AsyncMolgenisRequest req, AsyncMolgenisResponse res) throws ParseException,
			DatabaseException, IOException
	{
		// HttpServletRequest request = req.getRequest();
		HttpServletResponse response = res.getResponse();
		UUID id = req.getLoadingScreenId();

		try
		{
			PrintWriter out = response.getWriter();

			if (mc.getLoadingScreenUUIDFactory().isActiveLoadingScreenId(id))
			{
				out.println("<html><head><META HTTP-EQUIV=Refresh CONTENT=\"10; URL=" + "loadingscreen?id="
						+ id.toString() + "\"></head><body>");
				out.println("LOADING");
				out.println("</body></html>");
			}
			else
			{
				LoadingScreen lscreen = mc.getLoadingScreenUUIDFactory().doneLoadingId(id);
				out.println("<html><head><META HTTP-EQUIV=Refresh CONTENT=\"10; URL=" + lscreen.service + "?id="
						+ id.toString() + "\"></head><body>");
				out.print(lscreen.output);
				out.println("</body></html>");
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new DatabaseException(e);
		}
	}

	@Override
	public void handleAsyncRequest(AsyncMolgenisRequest request, UUID id)
	{
		// loading screen itself is not async ofcourse

	}

}
