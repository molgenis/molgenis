package org.molgenis.securityui.controller;

import static java.util.Objects.requireNonNull;

import org.molgenis.web.PluginController;
import org.molgenis.web.menu.MenuReaderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(SecurityUiController.URI)
public class SecurityUiController extends PluginController {
  public static final String ID = "security-ui";
  public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;
  private static final String KEY_BASE_URL = "baseUrl";

  static final String VIEW_TEMPLATE = "view-security-ui";
  private final MenuReaderService menuReaderService;

  SecurityUiController(MenuReaderService menuReaderService) {
    super(URI);
    this.menuReaderService = requireNonNull(menuReaderService);
  }

  @GetMapping("/**")
  public String init(Model model) {
    model.addAttribute(KEY_BASE_URL, menuReaderService.findMenuItemPath(ID));
    return VIEW_TEMPLATE;
  }
}
