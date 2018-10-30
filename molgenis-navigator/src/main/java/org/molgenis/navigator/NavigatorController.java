package org.molgenis.navigator;

import static java.util.Objects.requireNonNull;
import static org.molgenis.navigator.NavigatorController.URI;
import static org.molgenis.security.core.utils.SecurityUtils.getCurrentUsername;
import static org.springframework.http.HttpStatus.OK;

import com.google.gson.Gson;
import javax.validation.Valid;
import org.molgenis.core.ui.controller.VuePluginController;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.jobs.JobExecutor;
import org.molgenis.navigator.copy.CopyResourceRequest;
import org.molgenis.navigator.copy.job.CopyJobExecution;
import org.molgenis.navigator.copy.job.CopyJobExecutionFactory;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(URI)
public class NavigatorController extends VuePluginController {
  public static final String ID = "navigator";
  public static final String URI = PLUGIN_URI_PREFIX + ID;

  private final NavigatorService navigatorService;
  private final CopyJobExecutionFactory copyJobExecutionFactory;
  private final JobExecutor jobExecutor;

  public NavigatorController(
      MenuReaderService menuReaderService,
      AppSettings appSettings,
      UserAccountService userAccountService,
      NavigatorService navigatorService,
      CopyJobExecutionFactory copyJobExecutionFactory,
      JobExecutor jobExecutor) {
    super(URI, menuReaderService, appSettings, userAccountService);
    this.navigatorService = requireNonNull(navigatorService);
    this.copyJobExecutionFactory = requireNonNull(copyJobExecutionFactory);
    this.jobExecutor = requireNonNull(jobExecutor);
  }

  @GetMapping("/**")
  public String init(Model model) {
    super.init(model, ID);
    return "view-navigator";
  }

  @DeleteMapping("/delete")
  @ResponseStatus(OK)
  public void deleteItems(@RequestBody @Valid DeleteItemsRequest deleteItemsRequest) {
    navigatorService.deleteItems(
        deleteItemsRequest.getPackageIds(), deleteItemsRequest.getEntityTypeIds());
  }

  @PostMapping("/copy")
  @ResponseStatus(OK)
  public void copy(@RequestBody @Valid CopyResourceRequest request) {
    CopyJobExecution jobExecution = copyJobExecutionFactory.create();
    jobExecution.setResources(new Gson().toJson(request.getResources()));
    jobExecution.setTargetPackage(request.getTargetPackage());
    jobExecution.setUser(getCurrentUsername());
    jobExecutor.submit(jobExecution);
  }
}
