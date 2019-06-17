package org.molgenis.api.data.v3;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;

class Selection {
  static final Selection EMPTY_SELECTION = new Selection(emptyMap());
  static final Selection FULL_SELECTION = new Selection();

  private Map<String, Selection> itemSelections;

  Selection() {
    this.itemSelections = null;
  }

  Selection(Map<String, Selection> itemSelections) {
    requireNonNull(itemSelections);
    this.itemSelections = unmodifiableMap(new HashMap<>(itemSelections));
  }

  boolean hasItems() {
    boolean hasItems;
    if (itemSelections == null) {
      hasItems = true;
    } else {
      hasItems = !itemSelections.isEmpty();
    }
    return hasItems;
  }

  boolean hasItem(String item) {
    boolean hasItem;
    if (itemSelections == null) {
      hasItem = true;
    } else {
      hasItem = itemSelections.containsKey(item);
    }
    return hasItem;
  }

  Selection getSelection(String item) {
    Selection selection;
    if (itemSelections != null) {
      selection = itemSelections.get(item);
      if (selection == null) {
        selection = EMPTY_SELECTION;
      }
    } else {
      selection = EMPTY_SELECTION;
    }
    return selection;
  }

  @Override
  public String toString() {
    return "Selection{" + "items=" + itemSelections + '}';
  }
}
