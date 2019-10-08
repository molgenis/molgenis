package org.molgenis.app.controller;

import org.molgenis.core.ui.controller.AbstractStaticContentController;
import org.molgenis.web.PluginController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/** Controller that handles news page requests */
@Controller
@RequestMapping(NewsController.URI)
public class NewsController extends AbstractStaticContentController {
  public static final String ID = "news";
  public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

  public NewsController() {
    super(ID, URI);
  }
}
