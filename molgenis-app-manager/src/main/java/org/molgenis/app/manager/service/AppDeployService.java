package org.molgenis.app.manager.service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface AppDeployService
{
	/**
	 * Load javascript files from the file store
	 *
	 * @param uri      An uri belonging to an app
	 * @param fileName File name of the requested js file
	 * @param response a {@link HttpServletResponse} used to attach any found js files
	 */
	void loadJavascriptResources(String uri, String fileName, HttpServletResponse response) throws IOException;

	/**
	 * Load css files from the file store
	 *
	 * @param uri      An uri belonging to an app
	 * @param fileName File name of the requested css file
	 * @param response a {@link HttpServletResponse} used to attach any found css files
	 */
	void loadCSSResources(String uri, String fileName, HttpServletResponse response) throws IOException;
}
