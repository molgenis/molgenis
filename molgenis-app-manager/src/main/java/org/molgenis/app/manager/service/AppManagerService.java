package org.molgenis.app.manager.service;

import org.molgenis.app.manager.model.AppRequest;
import org.molgenis.app.manager.model.AppResponse;

import java.util.List;

public interface AppManagerService
{
	/**
	 * Retrieve a list of {@link AppResponse}s
	 */
	List<AppResponse> getApps();

	/**
	 * Retrieve an {@link AppResponse} based on an App id
	 *
	 * @param id The id of an App
	 * @return An {@link AppResponse}
	 */
	AppResponse getAppById(String id);

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
	 * Create an App
	 *
	 * @param appRequest An {@link AppRequest} used to create an app
	 */
	void createApp(AppRequest appRequest);

	/**
	 * Edit an existing App
	 *
	 * @param appRequest An {@link AppRequest} used to update an existing app
	 */
	void editApp(AppRequest appRequest);

	/**
	 * Delete an existing App
	 *
	 * @param id The id of an App
	 */
	void deleteApp(String id);
}
