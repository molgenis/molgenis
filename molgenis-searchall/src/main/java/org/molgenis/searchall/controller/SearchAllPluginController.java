package org.molgenis.searchall.controller;

import static org.molgenis.searchall.controller.SearchAllPluginController.URI;

import org.molgenis.core.ui.controller.VuePluginController;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.menu.MenuReaderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(URI)
public class SearchAllPluginController extends VuePluginController {
  public static final String ID = "searchAll";
  public static final String URI = PLUGIN_URI_PREFIX + ID;

  public static final String NAVIGATOR = "navigator";
  public static final String DATAEXPLORER = "dataexplorer";

  public SearchAllPluginController(
      AppSettings appSettings,
      MenuReaderService menuReaderService,
      UserAccountService userAccountService) {
    super(URI, menuReaderService, appSettings, userAccountService);
  }

  @GetMapping("/**")
  public String init(Model model) {
    super.init(model, ID);
    model.addAttribute("navigatorBaseUrl", menuReaderService.findMenuItemPath(NAVIGATOR));
    model.addAttribute("dataExplorerBaseUrl", menuReaderService.findMenuItemPath(DATAEXPLORER));
    return "view-search-all";
  }
}
