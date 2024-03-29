package org.molgenis.web.menu;

import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.plugin.model.PluginIdentity;
import org.molgenis.data.plugin.model.PluginPermission;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.menu.model.Menu;
import org.molgenis.web.menu.model.MenuGsonConfig;
import org.molgenis.web.menu.model.MenuItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {MenuGsonConfig.class})
class MenuReaderServiceImplTest extends AbstractMolgenisSpringTest {
  @Autowired private Gson gson;
  @Mock private UserPermissionEvaluator userPermissionEvaluator;
  @Mock private AppSettings appSettings;
  private MenuReaderServiceImpl menuReaderService;
  private Menu menu;

  @BeforeEach
  void beforeMethod() {
    MenuItem p30 = MenuItem.create("p3_0", "lbl");
    MenuItem p31 = MenuItem.create("p3_1", "lbl");

    Menu p20 = Menu.create("p2_0", "lbl", ImmutableList.of(p30, p31));
    MenuItem p21 = MenuItem.create("p2_1", "lbl");

    MenuItem p10 = MenuItem.create("p1_0", "lbl");
    Menu p11 = Menu.create("p1_1", "lbl", ImmutableList.of(p20, p21));

    menu = Menu.create("root", "", ImmutableList.of(p10, p11));

    this.menuReaderService = new MenuReaderServiceImpl(appSettings, gson, userPermissionEvaluator);
  }

  @Test
  void MenuReaderServiceImpl() {
    assertThrows(NullPointerException.class, () -> new MenuReaderServiceImpl(null, null, null));
  }

  @Test
  void findMenuItemPath() {
    String menuString = gson.toJson(menu);
    when(appSettings.getMenu()).thenReturn(menuString);
    when(userPermissionEvaluator.hasPermission(
            any(PluginIdentity.class), eq(PluginPermission.VIEW_PLUGIN)))
        .thenReturn(true);
    assertEquals("/menu/root/p1_0", menuReaderService.findMenuItemPath("p1_0"));
    assertEquals("/menu/root/p1_1", menuReaderService.findMenuItemPath("p1_1"));
    assertEquals("/menu/p1_1/p2_0", menuReaderService.findMenuItemPath("p2_0"));
    assertEquals("/menu/p1_1/p2_1", menuReaderService.findMenuItemPath("p2_1"));
    assertEquals("/menu/p2_0/p3_0", menuReaderService.findMenuItemPath("p3_0"));
    assertEquals("/menu/p2_0/p3_1", menuReaderService.findMenuItemPath("p3_1"));
    assertNull(menuReaderService.findMenuItemPath("non_existing"));
  }

  @Test
  void getMenu() {
    when(appSettings.getMenu())
        .thenReturn(
            "{\n"
                + "	\"type\": \"menu\",\n"
                + "	\"id\": \"menu\",\n"
                + "	\"label\": \"Menu\",\n"
                + "	\"items\": [{\n"
                + "		\"type\": \"plugin\",\n"
                + "		\"id\": \"plugin0\",\n"
                + "		\"label\": \"Plugin #0\",\n"
                + "		\"params\": \"a=0&b=1\"\n"
                + "	},\n"
                + "	{\n"
                + "		\"type\": \"menu\",\n"
                + "		\"id\": \"submenu\",\n"
                + "		\"label\": \"Submenu\",\n"
                + "		\"items\": [{\n"
                + "			\"type\": \"plugin\",\n"
                + "			\"id\": \"plugin1\",\n"
                + "			\"label\": \"Plugin #1\"\n"
                + "		}]\n"
                + "	}]\n"
                + "}");

    MenuItem plugin0 = MenuItem.create("plugin0", "Plugin #0", "a=0&b=1");
    MenuItem plugin1 = MenuItem.create("plugin1", "Plugin #1");
    Menu submenu = Menu.create("submenu", "Submenu", Collections.singletonList(plugin1));
    Menu menu = Menu.create("menu", "Menu", ImmutableList.of(plugin0, submenu));
    when(userPermissionEvaluator.hasPermission(any(), eq(PluginPermission.VIEW_PLUGIN)))
        .thenReturn(true);

    assertEquals(of(menu), menuReaderService.getMenu());
  }
}
