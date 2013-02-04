package org.molgenis.framework.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.molgenis.framework.ui.MolgenisOriginalStyle;

/**
 * Serves static files such as images, css files and javascript from classpath.
 * This is servlet is used when serving from a Jar file in the Mortbay server.
 * Using tomcat the static serving is left to the container.
 */
public class ResourceServlet extends HttpServlet
{
	private static final long serialVersionUID = 8579428014673624684L;
	private static final Logger logger = Logger.getLogger(ResourceServlet.class);

	/**
	 * Get a resource from the jar and copy it the the response.
	 */
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String resourcePath = request.getRequestURI().substring(request.getContextPath().length() + 1);
		// logger.debug("retrieving file " + resourcePath);
		InputStream in = null;
		try
		{
			if (resourcePath.startsWith("generated-res"))
			{
				// strip the 'generated-
				URLConnection conn = MolgenisOriginalStyle.class.getResource(resourcePath.substring(10))
						.openConnection();
				// File file = conn.
				// logger.debug("serving file " + conn);
				// URLConnection conn = file.openConnection();
				in = new BufferedInputStream(conn.getInputStream());

				// String mimetype = new
				// MimetypesFileTypeMap().getContentType(resourcePath);
				// logger.debug("mimetype for " + resourcePath + ": " +
				// mimetype);
				// response.setContentType(mimetype);
				response.setHeader("Cache-Control", "max-age=0"); // allow some
																	// client
																	// side
																	// caching
				// strict

				OutputStream out = response.getOutputStream();
				byte[] buffer = new byte[2048];
				for (;;)
				{
					int nBytes = in.read(buffer);
					if (nBytes <= 0) break;
					out.write(buffer, 0, nBytes);
				}
				out.flush();
				out.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("loading of failed: " + e);
		}
		finally
		{
			IOUtils.closeQuietly(in);
		}

	}
}
