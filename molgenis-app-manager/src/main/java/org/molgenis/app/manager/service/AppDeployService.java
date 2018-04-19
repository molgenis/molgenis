package org.molgenis.app.manager.service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface AppDeployService
{
	/**
	 * Transforms any js or css links to be served from the baseUrl
	 *
	 * @param template The html template provided by the user
	 * @param baseUrl  The base URL of the app currently being loaded
	 * @return A transformed template with correct resource references
	 */
	String configureTemplateResourceReferencing(String template, String baseUrl);

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

	/**
	 * Load images from the file store
	 *
	 * @param uri      An uri belonging to an app
	 * @param fileName File name of the requested image file
	 * @param response a {@link HttpServletResponse} used to attach any found image files
	 */
	void loadImageResources(String uri, String fileName, HttpServletResponse response) throws IOException;
}
