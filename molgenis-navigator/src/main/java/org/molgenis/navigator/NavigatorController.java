package org.molgenis.navigator;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static org.molgenis.navigator.NavigatorController.URI;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.validation.Valid;
import org.molgenis.core.ui.controller.VuePluginController;
import org.molgenis.core.ui.menu.Menu;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.navigator.model.Resource;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(URI)
public class NavigatorController extends VuePluginController {
  public static final String ID = "navigator";
  public static final String URI = PLUGIN_URI_PREFIX + ID;

  private final NavigatorService navigatorService;

  NavigatorController(
      MenuReaderService menuReaderService,
      AppSettings appSettings,
      UserAccountService userAccountService,
      NavigatorService navigatorService) {
    super(URI, menuReaderService, appSettings, userAccountService);
    this.navigatorService = requireNonNull(navigatorService);
  }

  @GetMapping("/**")
  public String init(Model model) {
    super.init(model, ID);

    Menu menu = menuReaderService.getMenu();
    asList("dataexplorer", "metadata-manager", "importwizard")
        .forEach(
            pluginId ->
                model.addAttribute(pluginId.replace('-', '_'), menu.findMenuItemPath(pluginId)));

    return "view-navigator";
  }

  @GetMapping("/get")
  @ResponseBody
  public GetResourcesResponse getResources(
      @RequestParam(value = "folderId", required = false) @Nullable @CheckForNull String folderId) {
    Folder folder = navigatorService.getFolder(folderId);
    List<Resource> resources = navigatorService.getResources(folderId);
    return GetResourcesResponse.create(folder, resources);
  }

  @GetMapping("/search")
  @ResponseBody
  public SearchResourcesResponse searchResources(@RequestParam(value = "query") String query) {
    List<Resource> resources = navigatorService.findResources(query);
    return SearchResourcesResponse.create(resources);
  }

  @PutMapping("/update")
  @ResponseStatus(OK)
  public void updateResource(@RequestBody @Valid UpdateResourceRequest updateResourceRequest) {
    navigatorService.updateResource(updateResourceRequest.getResource());
  }

  @PostMapping("/copy")
  @ResponseBody
  public JobExecution copyResources(@RequestBody @Valid CopyResourcesRequest copyResourcesRequest) {
    return navigatorService.copyResources(
        copyResourcesRequest.getResources(), copyResourcesRequest.getTargetFolderId());
  }

  @PostMapping("/download")
  @ResponseBody
  public JobExecution downloadResources(
      @RequestBody @Valid DownloadResourcesRequest downloadResourcesRequest) {
    return navigatorService.downloadResources(downloadResourcesRequest.getResources());
  }

  @PostMapping("/move")
  @ResponseStatus(OK)
  public void moveResources(@RequestBody @Valid MoveResourcesRequest moveResourcesRequest) {
    navigatorService.moveResources(
        moveResourcesRequest.getResources(), moveResourcesRequest.getTargetFolderId());
  }

  @DeleteMapping("/delete")
  @ResponseBody
  public JobExecution deleteResources(
      @RequestBody @Valid DeleteResourcesRequest deleteItemsRequest) {
    return navigatorService.deleteResources(deleteItemsRequest.getResources());
  }
}
