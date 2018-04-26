package org.molgenis.app.manager.service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface AppDeployService
{
	/**
	 * Load resources from a dynamic location
	 *
	 * @param fileName File name of the requested resource
	 * @param response a {@link HttpServletResponse} used to attach the requested resource file
	 */
	void loadResource(String fileName, HttpServletResponse response) throws IOException;
}
