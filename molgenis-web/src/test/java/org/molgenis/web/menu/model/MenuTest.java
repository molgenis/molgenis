package org.molgenis.web.menu.model;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.molgenis.web.menu.model.Menu.create;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.molgenis.util.AutoGson;

class MenuTest {
  private Menu menu;
  private Gson gson;

  @SuppressWarnings("unchecked")
  private Class<? extends Menu> menuAutoValueClass =
      (Class<? extends Menu>) Menu.class.getAnnotation(AutoGson.class).autoValueClass();

  @SuppressWarnings("unchecked")
  private Class<? extends MenuItem> menuItemAutoValueClass =
      (Class<? extends MenuItem>) MenuItem.class.getAnnotation(AutoGson.class).autoValueClass();

  @BeforeEach
  void setUp() {
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
  void testFilterMenuNodeFiltersAllChildren() {
    assertEquals(of(create("root", "", emptyList())), menu.filter(it -> false));
  }

  @Test
  void getPath() {
    assertEquals(of(asList("root/p1_0".split("/"))), menu.getPath("p1_0"));
    assertEquals(of(asList("root/p1_1".split("/"))), menu.getPath("p1_1"));
    assertEquals(of(asList("p1_1/p2_0".split("/"))), menu.getPath("p2_0"));
    assertEquals(of(asList("p1_1/p2_1".split("/"))), menu.getPath("p2_1"));
    assertEquals(of(asList("p2_0/p3_0".split("/"))), menu.getPath("p3_0"));
    assertEquals(of(asList("p2_0/p3_1".split("/"))), menu.getPath("p3_1"));
    assertEquals(empty(), menu.getPath("non_existing"));
  }

  @Test
  void toJsonAndBackAgain() {
    String json = gson.toJson(menu);
    assertEquals(menu, gson.fromJson(json, menuAutoValueClass));
  }
}
