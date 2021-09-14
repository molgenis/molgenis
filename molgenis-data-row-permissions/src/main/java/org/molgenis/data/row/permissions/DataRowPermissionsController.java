package org.molgenis.data.row.permissions;

import org.molgenis.web.PluginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.molgenis.data.row.permissions.DataRowPermissionsController.URI;

@Controller
@RequestMapping(URI)
public class DataRowPermissionsController extends PluginController {
    private static final Logger LOG = LoggerFactory.getLogger(DataRowPermissionsController.class);

    public static final String DATA_ROW_PERMISSIONS = "data-row-permissions";
    public static final String URI = PLUGIN_URI_PREFIX + DATA_ROW_PERMISSIONS;

    public DataRowPermissionsController() {
        super(URI);
    }

    @GetMapping("/**")
    public String init() {
        return "view-data-row-permissions";
    }
}