package org.molgenis.script;

import static java.util.Objects.requireNonNull;

import org.molgenis.web.PluginController;
import org.molgenis.web.menu.MenuReaderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(ScriptPluginController.URI)
public class ScriptPluginController extends PluginController {
  public static final String ID = "scripts";
  public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;
  private static final String KEY_BASE_URL = "baseUrl";

  private final MenuReaderService menuReaderService;

  public ScriptPluginController(MenuReaderService menuReaderService) {
    super(URI);
    this.menuReaderService = requireNonNull(menuReaderService);
  }

  @GetMapping("/**")
  public String listScripts(Model model) {
    model.addAttribute(KEY_BASE_URL, menuReaderService.findMenuItemPath(ID));
    return "view-scripts";
  }
}
