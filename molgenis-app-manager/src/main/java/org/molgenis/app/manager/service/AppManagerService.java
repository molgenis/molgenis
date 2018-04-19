package org.molgenis.app.manager.service;

import net.lingala.zip4j.exception.ZipException;
import org.molgenis.app.manager.model.AppResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AppManagerService
{
	/**
	 * Retrieve a list of {@link AppResponse}s
	 */
	List<AppResponse> getApps();

	/**
	 * Retrieve an {@link AppResponse} based a unique URI
	 * Throws an {@link org.molgenis.app.manager.exception.AppManagerException} when there is no app with the requested URI
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
	 * @param multipartFile A zip file with the new app
	 */
	void uploadApp(MultipartFile multipartFile) throws IOException, ZipException;
}
