package org.molgenis.web.menu.model;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.validation.constraints.NotEmpty;

public interface MenuNode {
  @NotEmpty
  String getId();

  @NotEmpty
  String getLabel();

  boolean isMenu();

  Optional<MenuNode> filter(Predicate<MenuNode> predicate);

  Optional<List<String>> getPath(String id);

  boolean contains(Predicate<MenuNode> predicate);

  Optional<MenuItem> firstItem();
}
