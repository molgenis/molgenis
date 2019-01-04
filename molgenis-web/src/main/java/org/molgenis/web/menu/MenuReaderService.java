package org.molgenis.web.menu;

import java.util.Optional;
import org.molgenis.web.menu.model.Menu;

public interface MenuReaderService {

  /** Retrieves the menu, filtered to show only the part you're permitted to view. */
  Optional<Menu> getMenu();

  /**
   * Find path leading up to a MenuItem.
   *
   * @param menuItemId ID of the MenuItem
   * @return path of the menu item, starting with menu/ or null if the menu item was not found
   */
  String findMenuItemPath(String menuItemId);
}
