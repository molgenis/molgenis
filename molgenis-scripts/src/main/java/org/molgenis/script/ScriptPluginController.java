package org.molgenis.script;

import org.molgenis.core.ui.controller.VuePluginController;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.PluginController;
import org.molgenis.web.menu.MenuReaderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(ScriptPluginController.URI)
public class ScriptPluginController extends VuePluginController {
  public static final String ID = "scripts";
  public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

  public ScriptPluginController(
      MenuReaderService menuReaderService,
      AppSettings appSettings,
      UserAccountService userAccountService) {
    super(URI, menuReaderService, appSettings, userAccountService);
  }

  @GetMapping("/**")
  public String listScripts(Model model) {
    super.init(model, ID);
    return "view-scripts";
  }
}
