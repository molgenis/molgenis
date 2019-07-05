package org.molgenis.api.model;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Selection {
  public static final Selection EMPTY_SELECTION = new Selection(emptyMap());
  public static final Selection FULL_SELECTION = new Selection();

  private Map<String, Selection> itemSelections;

  public Selection() {
    this.itemSelections = null;
  }

  public Selection(Map<String, Selection> itemSelections) {
    requireNonNull(itemSelections);
    this.itemSelections = unmodifiableMap(new HashMap<>(itemSelections));
  }

  public boolean hasItems() {
    boolean hasItems;
    if (itemSelections == null) {
      hasItems = true;
    } else {
      hasItems = !itemSelections.isEmpty();
    }
    return hasItems;
  }

  public boolean hasItem(String item) {
    boolean hasItem;
    if (itemSelections == null) {
      hasItem = true;
    } else {
      hasItem = itemSelections.containsKey(item);
    }
    return hasItem;
  }

  public Optional<Selection> getSelection(String item) {
    Selection selection = itemSelections != null ? itemSelections.get(item) : null;
    return Optional.ofNullable(selection);
  }

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder("Selection{");
    if (itemSelections == null) {
      stringBuilder.append("<all>");
    } else if (itemSelections.isEmpty()) {
      stringBuilder.append("<none>");
    } else {
      stringBuilder.append(itemSelections);
    }
    stringBuilder.append('}');
    return stringBuilder.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Selection selection = (Selection) o;
    return Objects.equals(itemSelections, selection.itemSelections);
  }

  @Override
  public int hashCode() {
    return Objects.hash(itemSelections);
  }
}
