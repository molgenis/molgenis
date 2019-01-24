package org.molgenis.securityui.controller;

import static org.molgenis.securityui.controller.SecurityUiController.URI;

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
@RequestMapping(URI)
public class SecurityUiController extends VuePluginController {
  public static final String ID = "security-ui";
  public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

  static final String VIEW_TEMPLATE = "view-security-ui";

  SecurityUiController(
      MenuReaderService menuReaderService,
      AppSettings appSettings,
      UserAccountService userAccountService) {
    super(URI, menuReaderService, appSettings, userAccountService);
  }

  @GetMapping("/**")
  public String init(Model model) {
    super.init(model, ID);
    return VIEW_TEMPLATE;
  }
}
