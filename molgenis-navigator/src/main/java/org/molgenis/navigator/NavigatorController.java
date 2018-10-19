package org.molgenis.navigator;

import static java.util.Objects.requireNonNull;
import static org.molgenis.navigator.NavigatorController.URI;
import static org.springframework.http.HttpStatus.OK;

import javax.validation.Valid;
import org.molgenis.core.ui.controller.VuePluginController;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.jobs.model.JobExecution;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    return "view-navigator";
  }

  @PostMapping("/copy")
  @ResponseBody
  public CopyResourcesResponse copyResources(
      @RequestBody @Valid CopyResourcesRequest copyResourcesRequest) {
    JobExecution jobExecution =
        navigatorService.copyResources(
            copyResourcesRequest.getResources(), copyResourcesRequest.getTargetFolderId());
    return CopyResourcesResponse.create(
        jobExecution.getIdentifier(), jobExecution.getStatus().toString());
  }

  @PostMapping("/move")
  @ResponseStatus(OK)
  public void moveResources(@RequestBody @Valid MoveResourcesRequest moveResourcesRequest) {
    navigatorService.moveResources(
        moveResourcesRequest.getResources(), moveResourcesRequest.getTargetFolderId());
  }

  @DeleteMapping("/delete")
  @ResponseStatus(OK)
  public void deleteItems(@RequestBody @Valid DeleteItemsRequest deleteItemsRequest) {
    navigatorService.deleteItems(
        deleteItemsRequest.getPackageIds(), deleteItemsRequest.getEntityTypeIds());
  }
}
