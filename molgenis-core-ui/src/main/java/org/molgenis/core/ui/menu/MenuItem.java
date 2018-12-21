package org.molgenis.core.ui.menu;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Objects;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class MenuItem {
  @NotNull
  @SerializedName("type")
  private MenuItemType type;

  @NotEmpty
  @SerializedName("id")
  private String id;

  @NotEmpty
  @SerializedName("label")
  private String label;

  @SerializedName("params")
  private String params;

  @SerializedName("items")
  private List<MenuItem> items;

  @SuppressWarnings("squid:S2637") // see GitHub issue #7859
  public MenuItem() {}

  public MenuItem(MenuItemType type, String id, String label) {
    this.type = type;
    this.id = id;
    this.label = label;
  }

  public MenuItemType getType() {
    return type;
  }

  public void setType(MenuItemType type) {
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getParams() {
    return params;
  }

  public void setParams(String params) {
    this.params = params;
  }

  public List<MenuItem> getItems() {
    return items;
  }

  public void setItems(List<MenuItem> items) {
    this.items = items;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MenuItem)) {
      return false;
    }
    MenuItem menuItem = (MenuItem) o;
    return getType() == menuItem.getType()
        && Objects.equals(getId(), menuItem.getId())
        && Objects.equals(getLabel(), menuItem.getLabel())
        && Objects.equals(getParams(), menuItem.getParams())
        && Objects.equals(getItems(), menuItem.getItems());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getType(), getId(), getLabel(), getParams(), getItems());
  }

  @Override
  public String toString() {
    return "MenuItem [type="
        + type
        + ", id="
        + id
        + ", label="
        + label
        + ", params="
        + params
        + ", items="
        + items
        + "]";
  }
}
