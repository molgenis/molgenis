package org.molgenis.searchall.controller;

import static java.util.Objects.requireNonNull;
import static org.molgenis.searchall.controller.SearchAllPluginController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.molgenis.core.ui.controller.VuePluginController;
import org.molgenis.searchall.model.Result;
import org.molgenis.searchall.service.SearchAllService;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.menu.MenuReaderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(URI)
public class SearchAllPluginController extends VuePluginController {
  public static final String ID = "searchAll";
  public static final String URI = PLUGIN_URI_PREFIX + ID;

  public static final String NAVIGATOR = "navigator";
  public static final String DATAEXPLORER = "dataexplorer";

  private final SearchAllService searchAllService;

  public SearchAllPluginController(
      SearchAllService searchAllService,
      AppSettings appSettings,
      MenuReaderService menuReaderService,
      UserAccountService userAccountService) {
    super(URI, menuReaderService, appSettings, userAccountService);
    this.searchAllService = requireNonNull(searchAllService);
  }

  @GetMapping("/**")
  public String init(Model model) {
    super.init(model, ID);
    model.addAttribute("navigatorBaseUrl", menuReaderService.findMenuItemPath(NAVIGATOR));
    model.addAttribute("dataExplorerBaseUrl", menuReaderService.findMenuItemPath(DATAEXPLORER));
    return "view-search-all";
  }

  @GetMapping(value = "/search", produces = APPLICATION_JSON_VALUE)
  @ResponseBody
  public Result searchAll(@RequestParam(value = "term") String searchterm) {
    return searchAllService.searchAll(searchterm);
  }
}
