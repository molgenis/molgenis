package org.molgenis.web.menu.model;

import com.google.gson.TypeAdapterFactory;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import org.molgenis.gson.AutoGson;
import org.molgenis.gson.GsonConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(GsonConfig.class)
public class MenuGsonConfig {

  private static <T> Class<? extends T> getAutoValueClass(Class<? extends T> clazz) {
    AutoGson annotation = clazz.getAnnotation(AutoGson.class);
    var autoValueClass = annotation.autoValueClass();
    return (Class<? extends T>) autoValueClass;
  }

  @Bean
  public TypeAdapterFactory menuTypeAdapterFactory() {
    RuntimeTypeAdapterFactory<MenuNode> menuRuntimeTypeAdapterFactory =
        RuntimeTypeAdapterFactory.of(MenuNode.class, "type");
    menuRuntimeTypeAdapterFactory.registerSubtype(getAutoValueClass(MenuItem.class), "plugin");
    menuRuntimeTypeAdapterFactory.registerSubtype(getAutoValueClass(Menu.class), "menu");
    return menuRuntimeTypeAdapterFactory;
  }
}
