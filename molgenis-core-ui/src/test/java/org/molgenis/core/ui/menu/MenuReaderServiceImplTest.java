package org.molgenis.core.ui.menu;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import java.util.Collections;
import java.util.Optional;
import org.mockito.Mock;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.plugin.model.PluginPermission;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.settings.AppSettings;
import org.molgenis.web.converter.GsonConfig;
import org.molgenis.web.menu.MenuReaderServiceImpl;
import org.molgenis.web.menu.model.Menu;
import org.molgenis.web.menu.model.MenuItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

@ContextConfiguration(classes = {GsonConfig.class})
public class MenuReaderServiceImplTest extends AbstractMolgenisSpringTest {
  @Autowired private Gson gson;

  @Mock private UserPermissionEvaluator userPermissionEvaluator;

  @Test(expectedExceptions = NullPointerException.class)
  public void MenuReaderServiceImpl() {
    new MenuReaderServiceImpl(null, null, null);
  }

  @Test
  public void getMenu() {
    AppSettings appSettings =
        when(mock(AppSettings.class).getMenu())
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
                    + "}")
            .getMock();

    MenuItem plugin0 = MenuItem.create("plugin0", "Plugin #0", "a=0&b=1");
    MenuItem plugin1 = MenuItem.create("plugin1", "Plugin #1");
    Menu submenu = Menu.create("submenu", "Submenu", Collections.singletonList(plugin1));
    Menu menu = Menu.create("menu", "Menu", ImmutableList.of(plugin0, submenu));
    when(userPermissionEvaluator.hasPermission(any(), eq(PluginPermission.VIEW_PLUGIN)))
        .thenReturn(true);

    assertEquals(
        new MenuReaderServiceImpl(appSettings, gson, userPermissionEvaluator).getMenu(),
        Optional.of(menu));
  }
}
