package org.molgenis.core.ui.controller;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

import java.io.UnsupportedEncodingException;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.PluginController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Plugin that redirects the user to another url.
 *
 * <p>Can be used to create a menu item to show a page outside molgenis
 *
 * <p>Usage: /plugin/redirect?url=http://www.mysite.nl
 */
@Controller
@RequestMapping(RedirectController.URI)
public class RedirectController extends PluginController {
  public static final String ID = "redirect";
  public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

  public final AppSettings appSettings;

  public RedirectController(AppSettings appSettings) {
    super(URI);
    this.appSettings = requireNonNull(appSettings);
  }

  @GetMapping
  public View redirect(@RequestParam("url") String url) throws UnsupportedEncodingException {

    if (!appSettings.getMenu().contains(encode(url, UTF_8.toString()))) {
      throw new IllegalArgumentException("Unkown url");
    }

    return new RedirectView(url, false, false, false);
  }
}
