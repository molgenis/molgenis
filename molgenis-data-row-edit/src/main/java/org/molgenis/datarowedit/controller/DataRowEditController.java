package org.molgenis.datarowedit.controller;

import static org.molgenis.datarowedit.controller.DataRowEditController.URI;

import org.molgenis.core.ui.controller.VuePluginController;
import org.molgenis.core.ui.menu.MenuReaderService;
import org.molgenis.dataexplorer.controller.DataExplorerController;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.PluginController;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(URI)
public class DataRowEditController extends VuePluginController {
  public static final String ID = "data-row-edit";
  public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

  public static final String VIEW_TEMPLATE = "view-data-row-edit";

  private AppSettings appSettings;

  DataRowEditController(
      MenuReaderService menuReaderService,
      AppSettings appSettings,
      UserAccountService userAccountService) {
    super(URI, menuReaderService, appSettings, userAccountService);
    this.appSettings = appSettings;
  }

  @GetMapping("/**")
  public String init(Model model) {
    model.addAttribute("baseUrl", URI);
    model.addAttribute("lng", LocaleContextHolder.getLocale().getLanguage());
    model.addAttribute("fallbackLng", appSettings.getLanguageCode());
    model.addAttribute("dataExplorerBaseUrl", getBaseUrl(DataExplorerController.ID));
    return VIEW_TEMPLATE;
  }
}
