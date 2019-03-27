package org.molgenis.app.manager.controller;

import static java.io.File.separator;
import static java.util.Objects.requireNonNull;
import static org.molgenis.app.manager.service.impl.AppManagerServiceImpl.APPS_DIR;
import static org.molgenis.app.manager.service.impl.AppManagerServiceImpl.ZIP_CONFIG_FILE;
import static org.molgenis.app.manager.service.impl.AppManagerServiceImpl.ZIP_INDEX_FILE;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.molgenis.app.manager.exception.CouldNotUploadAppException;
import org.molgenis.app.manager.meta.App;
import org.molgenis.app.manager.meta.AppMetadata;
import org.molgenis.app.manager.model.AppConfig;
import org.molgenis.app.manager.model.AppResponse;
import org.molgenis.app.manager.service.AppManagerService;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.web.PluginController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping(AppManagerController.URI)
public class AppManagerController extends PluginController {
  public static final String ID = "appmanager";
  public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

  private final AppManagerService appManagerService;
  private final DataService dataService;

  public AppManagerController(AppManagerService appManagerService, DataService dataService) {
    super(URI);
    this.appManagerService = requireNonNull(appManagerService);
    this.dataService = requireNonNull(dataService);
  }

  @GetMapping
  public String init() {
    return "view-app-manager";
  }

  @ResponseBody
  @GetMapping("/apps")
  public List<AppResponse> getApps() {
    return appManagerService.getApps();
  }

  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/activate/{id}")
  public void activateApp(@PathVariable(value = "id") String id) {
    App app = getApp(id);
    app.setActive(true);
    dataService.update(AppMetadata.APP, app);
  }

  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/deactivate/{id}")
  public void deactivateApp(@PathVariable(value = "id") String id) {
    App app = getApp(id);
    app.setActive(false);
    dataService.update(AppMetadata.APP, app);
  }

  @ResponseStatus(HttpStatus.OK)
  @DeleteMapping("/delete/{id}")
  public void deleteApp(@PathVariable("id") String id) {
    dataService.deleteById(AppMetadata.APP, id);
  }

  @ResponseStatus(HttpStatus.OK)
  @PostMapping("/upload")
  public void uploadApp(@RequestParam("file") MultipartFile multipartFile) {
    String filename = multipartFile.getOriginalFilename();
    String formFieldName = multipartFile.getName();
    try (InputStream fileInputStream = multipartFile.getInputStream()) {
      String tempDir = appManagerService.uploadApp(fileInputStream, filename, formFieldName);
      String configFile = appManagerService.extractFileContent(tempDir, ZIP_CONFIG_FILE);
      AppConfig appConfig = appManagerService.checkAndObtainConfig(tempDir, configFile);
      String htmlTemplate =
          appManagerService.extractFileContent(
              APPS_DIR + separator + appConfig.getName(), ZIP_INDEX_FILE);
      appManagerService.configureApp(appConfig, htmlTemplate);
    } catch (IOException err) {
      throw new CouldNotUploadAppException(filename);
    }
  }

  private App getApp(String id) {
    App app = dataService.findOneById(AppMetadata.APP, id, App.class);
    if (app == null) {
      throw new UnknownEntityException(AppMetadata.APP, id);
    }
    return app;
  }
}
