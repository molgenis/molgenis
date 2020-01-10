package org.molgenis.settings.controller;

import static java.util.Objects.requireNonNull;

import org.molgenis.web.PluginController;
import org.molgenis.web.menu.MenuReaderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(SettingsController.URI)
public class SettingsController extends PluginController {

  public static final String ID = "settings";
  public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

  public static final String VIEW_TEMPLATE = "view-settings";
  private final MenuReaderService menuReaderService;

  SettingsController(MenuReaderService menuReaderService) {
    super(URI);
    this.menuReaderService = requireNonNull(menuReaderService);
  }

  @GetMapping("/**")
  public String init(Model model) {
    model.addAttribute(KEY_BASE_URL, menuReaderService.findMenuItemPath(ID));
    return VIEW_TEMPLATE;
  }
}
