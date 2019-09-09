package org.molgenis.core.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.molgenis.data.plugin.model.PluginMetadata.PLUGIN;
import static org.springframework.web.servlet.HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownPluginException;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.web.menu.MenuReaderService;
import org.molgenis.web.menu.model.Menu;
import org.molgenis.web.menu.model.MenuItem;
import org.springframework.ui.Model;

class MolgenisMenuControllerTest extends AbstractMockitoTest {
  private MolgenisMenuController molgenisMenuController;

  @Mock private DataService dataService;
  @Mock private MenuReaderService menuReaderService;
  @Mock private Plugin plugin;
  @Mock private Model model;
  @Mock private HttpServletRequest request;
  private Menu menu;

  @BeforeEach
  void beforeMethod() {
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
  void testForwardDefaultMenuPlugin() {
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

  @Test
  void testForwardDefaultMenuPluginNoMenu() {
    when(menuReaderService.getMenu()).thenReturn(Optional.empty());

    assertThrows(
        RuntimeException.class,
        () -> molgenisMenuController.forwardDefaultMenuDefaultPlugin(model));
  }

  @Test
  void testForwardDefaultMenuPluginNoPermissions() {
    when(menuReaderService.getMenu()).thenReturn(menu.filter(it -> false).map(Menu.class::cast));

    assertEquals(molgenisMenuController.forwardDefaultMenuDefaultPlugin(model), "forward:/login");

    verify(model).addAttribute("menu_id", "main");
    verifyNoMoreInteractions(model);
  }

  @Test
  void testForwardSpecificMenuDefaultPlugin() {
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

  @Test
  void testForwardSpecificMenuDoesNotExist() {
    when(menuReaderService.getMenu()).thenReturn(Optional.of(menu));

    assertThrows(
        RuntimeException.class,
        () -> molgenisMenuController.forwardMenuDefaultPlugin("nonexisting", model));
  }

  @Test
  void testForwardSpecificMenuNoPermissionsForwardsToVoidController() {
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
  void testForwardSpecificMenuSpecificPluginDoesNotCheckMenu() {
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

  @Test
  void testGetForwardPluginUriUnknownPlugin() {
    String pluginId = "pluginA";
    when(dataService.findOneById(PLUGIN, pluginId, Plugin.class)).thenReturn(null);

    Exception exception =
        assertThrows(
            UnknownPluginException.class,
            () -> molgenisMenuController.getForwardPluginUri(pluginId, null, null));
    assertThat(exception.getMessage()).containsPattern("id:pluginA");
  }

  @Test
  void testGetForwardPluginUri() {
    String pluginId = "pluginB";
    when(plugin.getPath()).thenReturn("app/test");
    when(dataService.findOneById(PLUGIN, pluginId, Plugin.class)).thenReturn(plugin);

    String uri = molgenisMenuController.getForwardPluginUri(pluginId, "", null);

    assertEquals(uri, "forward:/plugin/app/test");
  }

  @Test
  void testGetForwardPluginPathRemainder() {
    String pluginId = "pluginId";
    String pluginPath = "pluginPath";
    when(plugin.getPath()).thenReturn(pluginPath);
    when(dataService.findOneById(PLUGIN, pluginId, Plugin.class)).thenReturn(plugin);
    assertEquals(
        molgenisMenuController.getForwardPluginUri(pluginId, "remainder", null),
        "forward:/plugin/pluginPath/remainder");
  }

  @Test
  void testGetForwardPluginUriQueryString() {
    String pluginId = "pluginId";
    String pluginPath = "pluginPath";
    when(plugin.getPath()).thenReturn(pluginPath);
    when(dataService.findOneById(PLUGIN, pluginId, Plugin.class)).thenReturn(plugin);
    assertEquals(
        molgenisMenuController.getForwardPluginUri(pluginId, null, "key=value"),
        "forward:/plugin/pluginPath?key=value");
  }

  @Test
  void testGetForwardPluginUriQueryStringEmpty() {
    String pluginId = "pluginId";
    String pluginPath = "pluginPath";
    when(plugin.getPath()).thenReturn(pluginPath);
    when(dataService.findOneById(PLUGIN, pluginId, Plugin.class)).thenReturn(plugin);
    assertEquals(
        molgenisMenuController.getForwardPluginUri(pluginId, null, ""),
        "forward:/plugin/pluginPath");
  }

  @Test
  void testGetForwardAppPlugin() {
    String pluginId = "app-pluginId";
    String pluginPath = "pluginPath";
    when(plugin.getPath()).thenReturn(pluginPath);
    when(dataService.findOneById(PLUGIN, pluginId, Plugin.class)).thenReturn(plugin);
    assertEquals(
        molgenisMenuController.getForwardPluginUri(pluginId, null, null),
        "forward:/plugin/pluginPath/");
  }

  @Test
  void testVoidController() {
    assertEquals(new MolgenisMenuController.VoidPluginController().init(), "view-void");
  }
}
