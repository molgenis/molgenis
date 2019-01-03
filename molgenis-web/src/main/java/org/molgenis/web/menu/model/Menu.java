package org.molgenis.web.menu.model;

import static java.util.stream.Collectors.toList;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.molgenis.util.AutoGson;

/** Menu grouping a list of child items. */
@AutoGson(autoValueClass = AutoValue_Menu.class)
@AutoValue
@SuppressWarnings("squid:S1610") // Autovalue class cannot be an interface
public abstract class Menu implements MenuNode {

  /** @return the child items grouped in this menu */
  public abstract List<MenuNode> getItems();

  @Override
  public Optional<MenuNode> filter(Predicate<MenuNode> predicate) {
    List<MenuNode> filteredItems =
        getItems()
            .stream()
            .map(item -> item.filter(predicate))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(toList());
    return Optional.of(create(getId(), getLabel(), filteredItems))
        .filter(predicate)
        .map(MenuNode.class::cast);
  }

  @Override
  public boolean isMenu() {
    return true;
  }

  private boolean containsMenuItem(MenuNode it, String itemId) {
    if (it instanceof MenuItem) {
      return it.getId().equals(itemId);
    } else {
      return it.getId().equals(itemId) || !((Menu) it).getItems().isEmpty();
    }
  }

  @Override
  public Optional<List<String>> getPath(String id) {
    return id.equals(getId())
        ? Optional.of(ImmutableList.of(id))
        : filter(menu -> containsMenuItem(menu, id))
            .flatMap(child -> ((Menu) child).getItems().get(0).getPath(id))
            .map(this::prefixId);
  }

  private List<String> prefixId(List<String> path) {
    if (path.size() == 2) {
      return path;
    }
    Builder<String> result = ImmutableList.builder();
    result.add(getId());
    result.addAll(path);
    return result.build();
  }

  @Override
  public boolean contains(Predicate<MenuNode> predicate) {
    return predicate.test(this) || getItems().stream().anyMatch(predicate);
  }

  public Optional<MenuItem> firstItem() {
    return getItems()
        .stream()
        .map(MenuNode::firstItem)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
  }

  public Optional<Menu> findMenu(String menuId) {
    if (menuId.equals(getId())) {
      return Optional.of(this);
    }
    for (MenuNode menuItem : getItems()) {
      if (menuItem instanceof Menu) {
        Optional<Menu> submenu = ((Menu) menuItem).findMenu(menuId);
        if (submenu.isPresent()) {
          return submenu;
        }
      }
    }
    return Optional.empty();
  }

  public static Menu create(String id, String label, List<MenuNode> items) {
    return new AutoValue_Menu(id, label, items);
  }
}
