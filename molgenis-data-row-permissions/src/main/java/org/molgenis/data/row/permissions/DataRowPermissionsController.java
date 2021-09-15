package org.molgenis.data.row.permissions;

import static org.molgenis.data.row.permissions.DataRowPermissionsController.URI;

import org.molgenis.web.PluginController;
import org.molgenis.web.menu.MenuReaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(URI)
public class DataRowPermissionsController extends PluginController {
  private static final Logger LOG = LoggerFactory.getLogger(DataRowPermissionsController.class);

  public static final String ID = "data-row-permissions";
  public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;
  public static final String VIEW_TEMPLATE = "view-data-row-permissions";

  public DataRowPermissionsController() {
    super(URI);
    LOG.info("I'm getting accessed!");
  }

  @GetMapping("/**")
  public String init() {
    return VIEW_TEMPLATE;
  }
}
