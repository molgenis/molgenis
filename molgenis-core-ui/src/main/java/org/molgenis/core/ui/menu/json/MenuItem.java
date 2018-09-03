package org.molgenis.core.ui.menu.json;

import com.google.auto.value.AutoValue;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;
import org.molgenis.web.UiMenu;
import org.molgenis.web.UiMenuItem;
import org.molgenis.web.UiMenuItemType;

@AutoValue
@AutoGson(autoValueClass = AutoValue_MenuItem.class)
@SuppressWarnings(
    "squid:S1610") // Abstract classes without fields should be converted to interfaces
public abstract class MenuItem {
  public static MenuItem create(
      String id, String label, String href, UiMenuItemType type, List<MenuItem> items) {
    return new AutoValue_MenuItem(id, label, href, type, items);
  }

  public static MenuItem create(UiMenuItem item) {
    List<MenuItem> items = null;
    if (item.getType() == UiMenuItemType.MENU) {
      UiMenu menu = (UiMenu) item;
      items =
          menu.getItems()
              .stream()
              .map(menuItem -> MenuItem.create(menuItem))
              .collect(Collectors.toList());
    }
    return new AutoValue_MenuItem(
        item.getId(), item.getName(), item.getUrl(), item.getType(), items);
  }

  abstract String getId();

  abstract String getLabel();

  @Nullable
  abstract String getHref();

  abstract UiMenuItemType getType();

  @Nullable
  abstract List<MenuItem> getItems();
}
