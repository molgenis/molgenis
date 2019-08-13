package org.molgenis.core.ui.controller;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.file.AccessDeniedException;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.molgenis.web.PluginController;
import org.molgenis.web.menu.MenuReaderService;
import org.molgenis.web.menu.model.MenuItem;
import org.molgenis.web.menu.model.MenuNode;
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

  private final MenuReaderService menuReaderService;

  public RedirectController(MenuReaderService menuReaderService) {
    super(URI);
    this.menuReaderService = menuReaderService;
  }

  /**
   * Indicates if a MenuNode is a redirect menu item with specified URL.
   *
   * @param node the MenuNode to test
   * @param url the URL that should match
   * @return boolean indicating if they match
   */
  static boolean nodeMatches(MenuNode node, String url) {
    return node instanceof MenuItem && itemMatches((MenuItem) node, url);
  }

  private static boolean itemMatches(MenuItem menuItem, String url) {
    return ID.equals(menuItem.getId()) && paramsMatch(menuItem.getParams(), url);
  }

  private static boolean paramsMatch(String params, String url) {
    NameValuePair expected = new BasicNameValuePair("url", url);
    List<NameValuePair> paramPairs = URLEncodedUtils.parse(params, UTF_8);
    return paramPairs.contains(expected);
  }

  @GetMapping
  public View redirect(@RequestParam("url") String url) throws AccessDeniedException {
    if (!menuReaderService
        .getMenu()
        .filter(menu -> menu.contains(node -> nodeMatches(node, url)))
        .isPresent()) {
      throw new AccessDeniedException("URL not present in menu!");
    }
    return new RedirectView(url, false, false, false);
  }
}
