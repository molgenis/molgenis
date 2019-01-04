package org.molgenis.web.menu.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import org.molgenis.util.AutoGson;

/** A menu item. */
@AutoValue
@AutoGson(autoValueClass = AutoValue_MenuItem.class)
@SuppressWarnings("squid:S1610") // Autovalue class cannot be an interface
public abstract class MenuItem implements MenuNode {

  /** @return the parameters for this menu item */
  @Nullable
  public abstract String getParams();

  @Override
  public Optional<MenuNode> filter(Predicate<MenuNode> predicate) {
    return Optional.of(this);
  }

  @Override
  public Optional<List<String>> getPath(String id) {
    return Optional.of(getId()).filter(id::equals).map(ImmutableList::of);
  }

  @Override
  public boolean contains(Predicate<MenuNode> predicate) {
    return predicate.test(this);
  }

  @Override
  public Optional<MenuItem> firstItem() {
    return Optional.of(this);
  }

  @Override
  public boolean isMenu() {
    return false;
  }

  /** @return the URL of this menu item, which is the combination of its ID and its parameters. */
  public String getUrl() {
    return Optional.ofNullable(getParams()).map(it -> getId() + "/" + it).orElse(getId());
  }

  public static MenuItem create(String id, String label) {
    return new AutoValue_MenuItem(id, label, null);
  }

  public static MenuItem create(String id, String label, String params) {
    return new AutoValue_MenuItem(id, label, params);
  }
}
