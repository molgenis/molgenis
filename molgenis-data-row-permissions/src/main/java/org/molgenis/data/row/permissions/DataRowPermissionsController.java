package org.molgenis.data.row.permissions;

import static org.molgenis.data.row.permissions.DataRowPermissionsController.URI;

import org.molgenis.settings.AppSettings;
import org.molgenis.web.PluginController;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(URI)
public class DataRowPermissionsController extends PluginController {

  public static final String ID = "data-row-permissions";
  public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;
  public static final String VIEW_TEMPLATE = "view-data-row-permissions";

  private final AppSettings appSettings;

  public DataRowPermissionsController(AppSettings appSettings) {
    super(URI);
    this.appSettings = appSettings;
  }

  @GetMapping("/**")
  public String init(Model model) {
    model.addAttribute("baseUrl", URI);
    model.addAttribute("lng", LocaleContextHolder.getLocale().getLanguage());
    model.addAttribute("fallbackLng", appSettings.getLanguageCode());
    return VIEW_TEMPLATE;
  }
}
