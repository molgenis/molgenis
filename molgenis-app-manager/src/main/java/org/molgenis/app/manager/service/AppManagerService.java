package org.molgenis.app.manager.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.molgenis.app.manager.exception.CouldNotDeleteAppException;
import org.molgenis.app.manager.exception.InvalidAppArchiveException;
import org.molgenis.app.manager.meta.App;
import org.molgenis.app.manager.model.AppConfig;
import org.molgenis.app.manager.model.AppResponse;

public interface AppManagerService {
  /** Retrieve a list of {@link AppResponse}s */
  List<AppResponse> getApps();

  /**
   * Retrieve an {@link AppResponse} based a unique URI
   *
   * @param appName The URI of an App
   * @return An {@link AppResponse}
   * @throws org.molgenis.app.manager.exception.AppForURIDoesNotExistException if there is no app
   *     with the requested URI
   */
  AppResponse getAppByName(String appName);

  /**
   * Activate an App by App id
   *
   * @param app The App
   */
  void activateApp(App app);

  /**
   * Deactivate an App by App id
   *
   * @param app The App
   */
  void deactivateApp(App app);

  /**
   * Delete an existing App
   *
   * @param id The id of an App
   * @throws CouldNotDeleteAppException if deletion fails
   */
  void deleteApp(String id);

  /**
   * Upload an app If zip fails to verify, throw an exception with the missing information
   *
   * @param zipData steam with app data in zip from
   * @param zipFileName the name of the zip file
   * @param formFieldName the value of the name field in the form
   * @return temporary directory for app
   * @throws IOException
   * @throws InvalidAppArchiveException if the zipData cannot be unzipped
   */
  String uploadApp(InputStream zipData, String zipFileName, String formFieldName)
      throws IOException;

  /**
   * Check the app-configuration and obtain the {@link AppConfig} if the configuration is valid
   *
   * @param tempDir temporary directory with uploadede app-content
   * @return appConfig
   * @throws IOException
   */
  AppConfig checkAndObtainConfig(String tempDir, String configContent) throws IOException;

  /**
   * Configure app in database.
   *
   * @param appConfig app configuration object
   * @param htmlTemplate HTML template based on the packaged index.html
   */
  void configureApp(AppConfig appConfig, String htmlTemplate);

  /**
   * Get the UTF-8 file-content of a file served by an app
   *
   * @param appDir app directory
   * @param fileName file name
   * @return UTF-8 file-content
   */
  String extractFileContent(String appDir, String fileName);
}
