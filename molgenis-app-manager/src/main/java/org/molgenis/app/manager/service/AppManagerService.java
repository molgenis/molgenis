package org.molgenis.app.manager.service;

import org.molgenis.app.manager.model.AppCreateRequest;
import org.molgenis.app.manager.model.AppEditRequest;
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
	 * @param appCreateRequest An {@link AppCreateRequest} used to create an app
	 */
	void createApp(AppCreateRequest appCreateRequest);

	/**
	 * Edit an existing App
	 *
	 * @param appEditRequest An {@link AppEditRequest} used to update an existing app
	 */
	void editApp(AppEditRequest appEditRequest);

	/**
	 * Delete an existing App
	 *
	 * @param id The id of an App
	 */
	void deleteApp(String id);
}
