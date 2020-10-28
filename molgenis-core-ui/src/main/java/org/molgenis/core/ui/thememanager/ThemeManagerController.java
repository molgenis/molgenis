package org.molgenis.core.ui.thememanager;

import org.molgenis.web.PluginController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(ThemeManagerController.URI)
public class ThemeManagerController extends PluginController {
  public static final String ID = "thememanager";
  public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

  public ThemeManagerController() {
    super(URI);
  }

  @GetMapping
  public String init(Model model) {
    return "view-thememanager";
  }
}
