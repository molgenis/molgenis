package org.molgenis.web.menu.model;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import java.util.Optional;
import org.molgenis.util.AutoGson;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MenuTest {
  private Menu menu;
  private Gson gson;

  @SuppressWarnings("unchecked")
  private Class<? extends Menu> menuAutoValueClass =
      (Class<? extends Menu>) Menu.class.getAnnotation(AutoGson.class).autoValueClass();

  @SuppressWarnings("unchecked")
  private Class<? extends MenuItem> menuItemAutoValueClass =
      (Class<? extends MenuItem>) MenuItem.class.getAnnotation(AutoGson.class).autoValueClass();

  @BeforeMethod
  public void setUp() {
    MenuItem p30 = MenuItem.create("p3_0", "lbl");
    MenuItem p31 = MenuItem.create("p3_1", "lbl");

    Menu p20 = Menu.create("p2_0", "lbl", ImmutableList.of(p30, p31));
    MenuItem p21 = MenuItem.create("p2_1", "lbl");

    MenuItem p10 = MenuItem.create("p1_0", "lbl");
    Menu p11 = Menu.create("p1_1", "lbl", ImmutableList.of(p20, p21));

    menu = Menu.create("root", "", ImmutableList.of(p10, p11));

    RuntimeTypeAdapterFactory<MenuNode> menuRuntimeTypeAdapterFactory =
        RuntimeTypeAdapterFactory.of(MenuNode.class, "type");

    menuRuntimeTypeAdapterFactory.registerSubtype(menuItemAutoValueClass, "plugin");
    menuRuntimeTypeAdapterFactory.registerSubtype(menuAutoValueClass, "menu");

    gson = new GsonBuilder().registerTypeAdapterFactory(menuRuntimeTypeAdapterFactory).create();
  }

  @Test
  public void testFilterMenuNodeFiltersAllChildren() {
    assertEquals(menu.filter(it -> false), Optional.of(Menu.create("root", "", emptyList())));
  }

  @Test
  public void getPath() {
    assertEquals(menu.getPath("p1_0"), Optional.of(asList("root/p1_0".split("/"))));
    assertEquals(menu.getPath("p1_1"), Optional.of(asList("root/p1_1".split("/"))));
    assertEquals(menu.getPath("p2_0"), Optional.of(asList("p1_1/p2_0".split("/"))));
    assertEquals(menu.getPath("p2_1"), Optional.of(asList("p1_1/p2_1".split("/"))));
    assertEquals(menu.getPath("p3_0"), Optional.of(asList("p2_0/p3_0".split("/"))));
    assertEquals(menu.getPath("p3_1"), Optional.of(asList("p2_0/p3_1".split("/"))));
    assertEquals(menu.getPath("non_existing"), Optional.empty());
  }

  @Test
  public void toJsonAndBackAgain() {
    String json = gson.toJson(menu);
    assertEquals(gson.fromJson(json, menuAutoValueClass), menu);
  }
}
