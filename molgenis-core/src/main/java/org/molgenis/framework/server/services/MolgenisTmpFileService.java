package org.molgenis.framework.server.services;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Hashtable;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.molgenis.framework.server.MolgenisContext;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.server.MolgenisResponse;
import org.molgenis.framework.server.MolgenisService;

/**
 * Serve files from tmp dir. TODO: create best version for this type of service
 * and use it everywhere. Though this one works OK. How it should really look:
 * /molgenis_apps/modules/webserver/core/servlets/Servlet.java
 * serveFile(HttpServletRequest req, HttpServletResponse res, boolean headOnly,
 * File file)
 */
public class MolgenisTmpFileService implements MolgenisService
{
	Logger logger = Logger.getLogger(MolgenisRapiService.class);
	Hashtable<String, Object> restParams;

	private MolgenisContext mc;

	public MolgenisTmpFileService(MolgenisContext mc)
	{
		this.mc = mc;
	}

	@Override
	public void handleRequest(MolgenisRequest request, MolgenisResponse response) throws IOException
	{
		String url = request.getRequest().getRequestURI();
		String variant = url.substring(url.indexOf("/") + 1, url.indexOf("/tmpfile"));
		InputStream in = null;
		OutputStream out = request.getResponse().getOutputStream();
		try
		{
			// get filename from used URL, so this is the only 'parameter'
			String urlBase = variant + "/tmpfile/";
			String urlFile = url.substring(urlBase.length() + 1);

			File tmpDir = new File(System.getProperty("java.io.tmpdir"));
			File filePath = new File(tmpDir.getAbsolutePath() + File.separatorChar + urlFile);

			URL localURL = filePath.toURI().toURL();
			URLConnection conn = localURL.openConnection();

			in = new BufferedInputStream(conn.getInputStream());

			// String mimetype = new
			// MimetypesFileTypeMap().getContentType(filePath);
			// logger.debug("mimetype for " + localURL + ": " + mimetype);
			// request.getResponse().setContentType(mimetype);

			String mimetype = mc.getServletContext().getMimeType(filePath.getName());
			if (mimetype != null) request.getResponse().setContentType(mimetype);

			request.getResponse().setContentLength((int) filePath.length());

			byte[] buffer = new byte[2048];
			for (;;)
			{
				int nBytes = in.read(buffer);
				if (nBytes <= 0) break;
				out.write(buffer, 0, nBytes);
			}
			out.flush();

			logger.info("serving " + request.getRequest().getRequestURI());
		}
		catch (Exception e)
		{
			byte[] header = ("Temporary file " + variant + " location error:\n").getBytes(Charset.forName("UTF-8"));
			out.write(header, 0, header.length);
			byte[] exception = ("loading of failed: " + e).getBytes(Charset.forName("UTF-8"));
			out.write(exception, 0, exception.length);
			logger.error("loading of failed: " + e);
		}
		finally
		{
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(in);
		}

	}
}