package org.molgenis.app.manager.service;

import net.lingala.zip4j.exception.ZipException;
import org.molgenis.app.manager.model.AppConfig;
import org.molgenis.app.manager.model.AppResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface AppManagerService
{
	/**
	 * Retrieve a list of {@link AppResponse}s
	 */
	List<AppResponse> getApps();

	/**
	 * Retrieve an {@link AppResponse} based a unique URI
	 * Throws an {@link org.molgenis.app.manager.exception.AppForURIDoesNotExistException} when there is no app with the requested URI
	 *
	 * @param uri The URI of an App
	 * @return An {@link AppResponse}
	 */
	AppResponse getAppByUri(String uri);

	/**
	 * Activate an App by App id
	 *
	 * @param id The id of an App
	 */
	void activateApp(String id);

	/**
	 * Deactivate an App by App id
	 *
	 * @param id The id of an App
	 */
	void deactivateApp(String id);

	/**
	 * Delete an existing App
	 *
	 * @param id The id of an App
	 */
	void deleteApp(String id) throws IOException;

	/**
	 * Upload an app
	 * If zip fails to verify, throw an exception with the missing information
	 *
	 * @param zipData       steam with app data in zip from
	 * @param zipFileName   the name of the zip file
	 * @param formFieldName the value of the name field in the form
	 * @return temporary directory for app
	 * @throws IOException
	 * @throws ZipException
	 */
	String uploadApp(InputStream zipData, String zipFileName, String formFieldName) throws IOException, ZipException;

	/**
	 * Check the app-configuration and obtain the {@link AppConfig}
	 * if the configuration is valid
	 *
	 * @param tempDir temporary directory with uploadede app-content
	 * @return appConfig
	 * @throws IOException
	 */
	AppConfig checkAndObtainConfig(String tempDir, String configContent) throws IOException;

	/**
	 *
	 * Configure app in database./§
	 *
	 * @param appConfig app configuration object
	 * @param htmlTemplate HTML template based on the packaged index.html
 	 */
	void configureApp(AppConfig appConfig, String htmlTemplate);

	/**
	 *
	 *
	 *
	 * @param appDir app directory
	 * @param fileName file name
	 * @return HTML template
	 */
	String extractFileContent(String appDir, String fileName);
}
