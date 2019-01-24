package org.molgenis.core.ui;

import static java.time.ZonedDateTime.now;
import static java.time.format.FormatStyle.MEDIUM;
import static java.util.Objects.requireNonNull;
import static org.molgenis.core.ui.MolgenisMenuController.URI;
import static org.molgenis.data.plugin.model.PluginMetadata.PLUGIN;
import static org.molgenis.security.core.runas.RunAsSystemAspect.runAsSystem;
import static org.molgenis.web.PluginAttributes.KEY_CONTEXT_URL;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownPluginException;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.web.PluginController;
import org.molgenis.web.menu.MenuReaderService;
import org.molgenis.web.menu.model.Menu;
import org.molgenis.web.menu.model.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerMapping;

/**
 * Responsibilities of this class: Looks up the plugin that will handle the request. Adds context
 * attributes to the Model so the freemarker view can use them when rendering the header and footer.
 */
@Controller
@RequestMapping(URI)
public class MolgenisMenuController {
  private static final Logger LOG = LoggerFactory.getLogger(MolgenisMenuController.class);

  public static final String URI = "/menu";

  private static final String KEY_MENU_ID = "menu_id";
  private static final String KEY_MOLGENIS_VERSION = "molgenis_version";
  private static final String KEY_MOLGENIS_BUILD_DATE = "molgenis_build_date";

  private final MenuReaderService menuReaderService;
  private final String molgenisVersion;
  private final String molgenisBuildData;
  private final DataService dataService;

  MolgenisMenuController(
      MenuReaderService menuReaderService,
      @Value("${molgenis.version}") String molgenisVersion,
      @Value("${molgenis.build.date}") String molgenisBuildData,
      DataService dataService) {
    this.dataService = requireNonNull(dataService);
    this.menuReaderService = requireNonNull(menuReaderService);
    this.molgenisVersion = requireNonNull(molgenisVersion, "molgenisVersion is null");
    requireNonNull(molgenisBuildData, "molgenisBuildDate is null");

    // workaround for Eclipse bug: https://github.com/molgenis/molgenis/issues/2667
    this.molgenisBuildData =
        molgenisBuildData.equals("${maven.build.timestamp}")
            ? DateTimeFormatter.ofLocalizedDateTime(MEDIUM).format(now()) + " by Eclipse"
            : molgenisBuildData;
  }

  /**
   * Forwards to the first plugin of the first menu that the user can read since no menu path is
   * provided.
   */
  @RequestMapping
  public String forwardDefaultMenuDefaultPlugin(Model model) {
    Menu menu =
        menuReaderService
            .getMenu()
            .orElseThrow(() -> new RuntimeException("main menu does not exist"));
    String menuId = menu.getId();
    model.addAttribute(KEY_MENU_ID, menuId);

    Optional<MenuItem> optionalActiveItem = menu.firstItem();
    if (!optionalActiveItem.isPresent()) {
      LOG.warn("main menu does not contain any (accessible) items");
      return "forward:/login";
    }
    MenuItem activeItem = optionalActiveItem.get();
    String pluginId = activeItem.getId();

    String contextUri = URI + '/' + menuId + '/' + pluginId;
    addModelAttributes(model, contextUri);

    return getForwardPluginUri(activeItem.getId(), null, getQueryString(activeItem));
  }

  private @Nullable @CheckForNull String getQueryString(MenuItem menuItem) {
    String pathRemainder;

    String url = Optional.ofNullable(menuItem.getParams()).orElse("");
    int index = url.indexOf('?');
    if (index != -1) {
      pathRemainder = url.substring(index + 1);
    } else {
      pathRemainder = null;
    }
    return pathRemainder;
  }

  /**
   * Forwards to the first menu item in the specified menu. Forwards to the void controller if the
   * user has no permissions to view anything in that menu, i.e. the menu is empty.
   *
   * @param menuId ID of the menu or plugin
   */
  @RequestMapping("/{menuId}")
  public String forwardMenuDefaultPlugin(@Valid @NotNull @PathVariable String menuId, Model model) {
    Menu filteredMenu =
        menuReaderService
            .getMenu()
            .flatMap(menu -> menu.findMenu(menuId))
            .orElseThrow(
                () -> new RuntimeException("menu with id [" + menuId + "] does not exist"));
    model.addAttribute(KEY_MENU_ID, menuId);

    String pluginId = filteredMenu.firstItem().map(MenuItem::getId).orElse(VoidPluginController.ID);

    String contextUri = URI + '/' + menuId + '/' + pluginId;
    addModelAttributes(model, contextUri);
    return getForwardPluginUri(pluginId);
  }

  private void addModelAttributes(Model model, String contextUri) {
    model.addAttribute(KEY_CONTEXT_URL, contextUri);
    model.addAttribute(KEY_MOLGENIS_VERSION, molgenisVersion);
    model.addAttribute(KEY_MOLGENIS_BUILD_DATE, molgenisBuildData);
  }

  /**
   * Forwards to the specified plugin in the specified menu. This may be a submenu. Only the last
   * two levels of the possibly very deep menu tree are used to construct the URL.
   *
   * @param menuId ID of the menu parent of the pluginID
   * @param pluginId ID of the plugin
   */
  @RequestMapping("/{menuId}/{pluginId}/**")
  public String forwardMenuPlugin(
      HttpServletRequest request,
      @Valid @NotNull @PathVariable String menuId,
      @Valid @NotNull @PathVariable String pluginId,
      Model model) {
    String contextUri = URI + '/' + menuId + '/' + pluginId;
    String mappingUri =
        (String) (request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE));
    String remainder = mappingUri.substring(contextUri.length());

    model.addAttribute(KEY_MENU_ID, menuId);
    addModelAttributes(model, contextUri);
    return getForwardPluginUri(pluginId, remainder);
  }

  private String getForwardPluginUri(String pluginId) {
    return getForwardPluginUri(pluginId, null);
  }

  private String getForwardPluginUri(
      String pluginId, @Nullable @CheckForNull String pathRemainder) {
    return getForwardPluginUri(pluginId, pathRemainder, null);
  }

  /**
   * Forwards to the plugin.
   *
   * @param pluginId ID of the plugin to forward to. Adds a trailing slash if this refers to the app
   *     controller
   * @param pathRemainder remainder of the path to add after the pluginID with / in between if it is
   *     nonempty
   * @param queryString query params to add to the forward url with ? in between if it is nonempty
   * @return forward URL String
   */
  String getForwardPluginUri(
      String pluginId,
      @Nullable @CheckForNull String pathRemainder,
      @Nullable @CheckForNull String queryString) {
    // get plugin path with elevated permissions because the anonymous user can also request plugins
    Plugin plugin = runAsSystem(() -> dataService.findOneById(PLUGIN, pluginId, Plugin.class));
    if (plugin == null) {
      throw new UnknownPluginException(pluginId);
    }

    StringBuilder strBuilder = new StringBuilder("forward:");
    strBuilder.append(PluginController.PLUGIN_URI_PREFIX).append(plugin.getPath());
    // If you do not append the trailing slash the queryString will be appended by an unknown code
    // snippet.
    // The trailing slash is needed for clients to serve resources 'relative' to the URI-path.
    if (pluginId.startsWith("app")) {
      strBuilder.append("/");
    }
    if (pathRemainder != null && !pathRemainder.isEmpty()) {
      strBuilder.append("/").append(pathRemainder);
    }
    if (queryString != null && !queryString.isEmpty()) {
      strBuilder.append('?').append(queryString);
    }
    return strBuilder.toString();
  }

  /** Controller used to render an empty page if the user has no permissions to view anything. */
  @Controller
  @RequestMapping(VoidPluginController.URI)
  public static class VoidPluginController extends PluginController {
    public static final String ID = "void";
    public static final String URI = PluginController.PLUGIN_URI_PREFIX + ID;

    public VoidPluginController() {
      super(URI);
    }

    @GetMapping
    public String init() {
      return "view-void";
    }
  }
}
