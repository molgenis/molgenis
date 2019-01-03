package org.molgenis.web.menu.model;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.validation.constraints.NotEmpty;

/**
 * Interface for the menu nodes in the menu tree. The menu nodes are either a menu or a menu item.
 */
public interface MenuNode {

  /** @return the ID of this menu node */
  @NotEmpty
  String getId();

  /** @return the label of this menu node to use when displaying the menu */
  @NotEmpty
  String getLabel();

  /** @return true if this is a menu, false if it is a menu item */
  boolean isMenu();

  /**
   * Filters this menu node and its items using a predicate.
   *
   * <p>Menus filter the item list using the predicate and then call the predicate to see if the
   * menu node matches the predicate or not.
   *
   * @param predicate {@link Predicate} to apply to the menu nodes
   * @return the filtered menu node or {@link Optional#empty()} if this menu does not match the
   *     predicate after filtering
   */
  Optional<MenuNode> filter(Predicate<MenuNode> predicate);

  /**
   * Returns the path from this menu node to the menu with given ID.
   *
   * @param id the ID of the menu to look for
   * @return Optional List of Strings of the IDs of the menus leading up to the specified id, or
   *     {@link Optional#empty()} if this menu does not contain a node with the specified ID
   */
  Optional<List<String>> getPath(String id);

  /**
   * Checks if this Menu node contains a menu node matching the given predicate
   *
   * @param predicate the predicate to match
   * @return boolean true if it contains a matching node, else false
   */
  boolean contains(Predicate<MenuNode> predicate);

  /**
   * Finds the first menu item in this menu node. If it is a menu, this is the first item of its
   * first child.
   */
  Optional<MenuItem> firstItem();
}
