package org.molgenis.datarowedit.controller;

import static java.util.Objects.requireNonNull;

import org.molgenis.core.ui.settings.FormSettings;
import org.molgenis.dataexplorer.controller.DataExplorerController;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.PluginController;
import org.molgenis.web.menu.MenuReaderService;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(DataRowEditController.URI)
public class DataRowEditController extends PluginController {
  public static final String ID = "data-row-edit";
  public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

  public static final String VIEW_TEMPLATE = "view-data-row-edit";
  private final MenuReaderService menuReaderService;

  private final AppSettings appSettings;
  private final FormSettings formSettings;

  DataRowEditController(
      MenuReaderService menuReaderService, AppSettings appSettings, FormSettings formSettings) {
    super(URI);
    this.appSettings = requireNonNull(appSettings);
    this.formSettings = requireNonNull(formSettings);
    this.menuReaderService = requireNonNull(menuReaderService);
  }

  @GetMapping("/**")
  public String init(Model model) {
    model.addAttribute("baseUrl", URI);
    model.addAttribute("lng", LocaleContextHolder.getLocale().getLanguage());
    model.addAttribute("fallbackLng", appSettings.getLanguageCode());
    model.addAttribute(
        "dataExplorerBaseUrl", menuReaderService.findMenuItemPath(DataExplorerController.ID));
    model.addAttribute("formSettings", formSettings);
    return VIEW_TEMPLATE;
  }
}
