package org.molgenis.app.manager.service;

import org.molgenis.app.manager.meta.App;
import org.molgenis.app.manager.model.AppRequest;

import java.util.List;

public interface AppManagerService
{
	List<App> getApps();

	App getAppById(String id);

	void activateApp(String id);

	void deactivateApp(String id);

	void createApp(AppRequest appRequest);

	void editApp(AppRequest appRequest);

	void deleteApp(String id);
}
