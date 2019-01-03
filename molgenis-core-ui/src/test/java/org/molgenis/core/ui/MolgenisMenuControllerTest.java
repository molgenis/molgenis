package org.molgenis.core.ui;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.molgenis.data.plugin.model.PluginMetadata.PLUGIN;
import static org.springframework.web.servlet.HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownPluginException;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.web.menu.MenuReaderService;
import org.molgenis.web.menu.model.Menu;
import org.molgenis.web.menu.model.MenuItem;
import org.springframework.ui.Model;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MolgenisMenuControllerTest extends AbstractMockitoTest {
  private MolgenisMenuController molgenisMenuController;

  @Mock private DataService dataService;
  @Mock private MenuReaderService menuReaderService;
  @Mock private Plugin plugin;
  @Mock private Model model;
  @Mock private HttpServletRequest request;
  private Menu menu;

  @BeforeMethod
  public void beforeMethod() {
    molgenisMenuController =
        new MolgenisMenuController(menuReaderService, "1.2.3", "2019-01-02 01:11 UTC", dataService);

    MenuItem home = MenuItem.create("home", "Home");
    MenuItem navigator = MenuItem.create("navigator", "Navigator");
    MenuItem themes = MenuItem.create("themes", "Themes");
    MenuItem permissions = MenuItem.create("permissions", "Permissions");
    Menu admin = Menu.create("admin", "Admin", ImmutableList.of(themes, permissions));
    menu = Menu.create("main", "Home", ImmutableList.of(home, navigator, admin));
  }

  @Test
  public void testForwardDefaultMenuPlugin() {
    when(menuReaderService.getMenu()).thenReturn(Optional.of(menu));
    when(dataService.findOneById(PLUGIN, "home", Plugin.class)).thenReturn(plugin);
    when(plugin.getPath()).thenReturn("home");

    assertEquals(
        molgenisMenuController.forwardDefaultMenuDefaultPlugin(model), "forward:/plugin/home");

    verify(model).addAttribute("menu_id", "main");
    verify(model).addAttribute("context_url", "/menu/main/home");
    verify(model).addAttribute("molgenis_version", "1.2.3");
    verify(model).addAttribute("molgenis_build_date", "2019-01-02 01:11 UTC");
    verifyNoMoreInteractions(model);
  }

  @Test(expectedExceptions = RuntimeException.class)
  public void testForwardDefaultMenuPluginNoMenu() {
    when(menuReaderService.getMenu()).thenReturn(Optional.empty());

    molgenisMenuController.forwardDefaultMenuDefaultPlugin(model);
  }

  @Test
  public void testForwardDefaultMenuPluginNoPermissions() {
    when(menuReaderService.getMenu()).thenReturn(menu.filter(it -> false).map(Menu.class::cast));

    assertEquals(molgenisMenuController.forwardDefaultMenuDefaultPlugin(model), "forward:/login");

    verify(model).addAttribute("menu_id", "main");
    verifyNoMoreInteractions(model);
  }

  @Test
  public void testForwardSpecificMenuDefaultPlugin() {
    when(menuReaderService.getMenu()).thenReturn(Optional.of(menu));
    when(dataService.findOneById(PLUGIN, "themes", Plugin.class)).thenReturn(plugin);
    when(plugin.getPath()).thenReturn("themes");

    assertEquals(
        molgenisMenuController.forwardMenuDefaultPlugin("admin", model), "forward:/plugin/themes");

    verify(model).addAttribute("menu_id", "admin");
    verify(model).addAttribute("context_url", "/menu/admin/themes");
    verify(model).addAttribute("molgenis_version", "1.2.3");
    verify(model).addAttribute("molgenis_build_date", "2019-01-02 01:11 UTC");
    verifyNoMoreInteractions(model);
  }

  @Test(expectedExceptions = RuntimeException.class)
  public void testForwardSpecificMenuDoesNotExist() {
    when(menuReaderService.getMenu()).thenReturn(Optional.of(menu));

    molgenisMenuController.forwardMenuDefaultPlugin("nonexisting", model);
  }

  @Test
  public void testForwardSpecificMenuNoPermissionsForwardsToVoidController() {
    Optional<Menu> admin =
        menu.filter(it -> ImmutableList.of("admin", "").contains(it.getId())).map(Menu.class::cast);
    when(menuReaderService.getMenu()).thenReturn(admin);
    when(dataService.findOneById(PLUGIN, "void", Plugin.class)).thenReturn(plugin);
    when(plugin.getPath()).thenReturn("void");

    assertEquals(
        molgenisMenuController.forwardMenuDefaultPlugin("admin", model), "forward:/plugin/void");

    verify(model).addAttribute("menu_id", "admin");
    verify(model).addAttribute("context_url", "/menu/admin/void");
    verify(model).addAttribute("molgenis_version", "1.2.3");
    verify(model).addAttribute("molgenis_build_date", "2019-01-02 01:11 UTC");
    verifyNoMoreInteractions(model);
  }

  @Test
  public void testForwardSpecificMenuSpecificPluginDoesNotCheckMenu() {
    when(dataService.findOneById(PLUGIN, "permissions", Plugin.class)).thenReturn(plugin);
    when(plugin.getPath()).thenReturn("permissions");
    when(request.getAttribute(PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE))
        .thenReturn("/menu/admin/permissions/blah/blah?x=y&a=b");

    assertEquals(
        molgenisMenuController.forwardMenuPlugin(request, "admin", "permissions", model),
        "forward:/plugin/permissions//blah/blah?x=y&a=b");

    verify(model).addAttribute("menu_id", "admin");
    verify(model).addAttribute("context_url", "/menu/admin/permissions");
    verify(model).addAttribute("molgenis_version", "1.2.3");
    verify(model).addAttribute("molgenis_build_date", "2019-01-02 01:11 UTC");
    verifyNoMoreInteractions(model);
  }

  @Test(
      expectedExceptions = UnknownPluginException.class,
      expectedExceptionsMessageRegExp = "id:pluginA")
  public void testGetForwardPluginUriUnknownPlugin() {
    String pluginId = "pluginA";
    when(dataService.findOneById(PLUGIN, pluginId, Plugin.class)).thenReturn(null);
    molgenisMenuController.getForwardPluginUri(pluginId, null, null);
  }

  @Test
  public void testGetForwardPluginUri() {
    String pluginId = "pluginB";
    when(plugin.getPath()).thenReturn("app/test");
    when(dataService.findOneById(PLUGIN, pluginId, Plugin.class)).thenReturn(plugin);

    String uri = molgenisMenuController.getForwardPluginUri(pluginId, "", null);

    assertEquals(uri, "forward:/plugin/app/test");
  }

  @Test
  public void testGetForwardPluginPathRemainder() {
    String pluginId = "pluginId";
    String pluginPath = "pluginPath";
    when(plugin.getPath()).thenReturn(pluginPath);
    when(dataService.findOneById(PLUGIN, pluginId, Plugin.class)).thenReturn(plugin);
    assertEquals(
        molgenisMenuController.getForwardPluginUri(pluginId, "remainder", null),
        "forward:/plugin/pluginPath/remainder");
  }

  @Test
  public void testGetForwardPluginUriQueryString() {
    String pluginId = "pluginId";
    String pluginPath = "pluginPath";
    when(plugin.getPath()).thenReturn(pluginPath);
    when(dataService.findOneById(PLUGIN, pluginId, Plugin.class)).thenReturn(plugin);
    assertEquals(
        molgenisMenuController.getForwardPluginUri(pluginId, null, "key=value"),
        "forward:/plugin/pluginPath?key=value");
  }

  @Test
  public void testGetForwardPluginUriQueryStringEmpty() {
    String pluginId = "pluginId";
    String pluginPath = "pluginPath";
    when(plugin.getPath()).thenReturn(pluginPath);
    when(dataService.findOneById(PLUGIN, pluginId, Plugin.class)).thenReturn(plugin);
    assertEquals(
        molgenisMenuController.getForwardPluginUri(pluginId, null, ""),
        "forward:/plugin/pluginPath");
  }

  @Test
  public void testGetForwardAppPlugin() {
    String pluginId = "app-pluginId";
    String pluginPath = "pluginPath";
    when(plugin.getPath()).thenReturn(pluginPath);
    when(dataService.findOneById(PLUGIN, pluginId, Plugin.class)).thenReturn(plugin);
    assertEquals(
        molgenisMenuController.getForwardPluginUri(pluginId, null, null),
        "forward:/plugin/pluginPath/");
  }

  @Test
  public void testVoidController() {
    assertEquals(new MolgenisMenuController.VoidPluginController().init(), "view-void");
  }
}
