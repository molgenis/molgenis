package org.molgenis.app.manager.service.impl;

import org.molgenis.app.manager.service.AppDeployService;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static java.net.URLConnection.guessContentTypeFromName;

@Service
public class AppDeployServiceImpl implements AppDeployService
{
	@Override
	public void loadResource(String fileName, HttpServletResponse response) throws IOException
	{
		File requestedResource = new File(fileName);
		response.setContentType(guessMimeType(requestedResource.getName()));
		response.setContentLength((int) requestedResource.length());
		response.setHeader("Content-Disposition",
				"attachment; filename=" + requestedResource.getName().replace(" ", "_"));

		try (InputStream is = new FileInputStream(requestedResource))
		{
			FileCopyUtils.copy(is, response.getOutputStream());
		}
	}

	private static String guessMimeType(String fileName)
	{
		if (fileName.endsWith(".js")) return "application/javascript;charset=UTF-8";
		if (fileName.endsWith(".css")) return "text/css;charset=UTF-8";
		return guessContentTypeFromName(fileName);
	}
}
