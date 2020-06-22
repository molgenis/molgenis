package org.molgenis.web.menu.model;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.validation.constraints.NotEmpty;
import org.molgenis.util.AutoGson;

/** A menu item. */
@AutoValue
@AutoGson(autoValueClass = AutoValue_Link.class)
@SuppressWarnings("java:S1610") // Autovalue class cannot be an interface
public abstract class Link implements MenuNode {

  /** @return the parameters for this menu item */
  public abstract String getParams();

  @Override
  public @NotEmpty String getType() {
    return "link";
  }

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
    return Optional.of(MenuItem.create(this.getId(), this.getLabel(), this.getParams()));
  }

  @Override
  public boolean isMenu() {
    return false;
  }

  /** @return the URL of this menu item, which is param */
  public String getUrl() {
    return getParams();
  }

  public static Link create(String id, String label) {
    return new AutoValue_Link(id, label, null);
  }

  public static Link create(String id, String label, String params) {
    return new AutoValue_Link(id, label, params);
  }
}
