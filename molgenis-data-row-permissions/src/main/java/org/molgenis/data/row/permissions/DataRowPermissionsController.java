package org.molgenis.data.row.permissions;

import static org.molgenis.data.row.permissions.DataRowPermissionsController.URI;

import org.molgenis.web.PluginController;
import org.molgenis.web.menu.MenuReaderService;
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

  private final MenuReaderService menuReaderService;

  public DataRowPermissionsController(MenuReaderService menuReaderService) {
    super(URI);
    this.menuReaderService = menuReaderService;
  }

  @GetMapping("/**")
  public String init(Model model) {
    model.addAttribute("baseUrl", menuReaderService.findMenuItemPath(ID));
    return VIEW_TEMPLATE;
  }
}
