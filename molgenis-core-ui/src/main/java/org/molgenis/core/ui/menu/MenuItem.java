package org.molgenis.core.ui.menu;

import com.google.gson.annotations.SerializedName;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

public class MenuItem
{
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

	public MenuItem()
	{
	}

	public MenuItem(MenuItemType type, String id, String label)
	{
		this.type = type;
		this.id = id;
		this.label = label;
	}

	public MenuItemType getType()
	{
		return type;
	}

	public void setType(MenuItemType type)
	{
		this.type = type;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public String getParams()
	{
		return params;
	}

	public void setParams(String params)
	{
		this.params = params;
	}

	public List<MenuItem> getItems()
	{
		return items;
	}

	public void setItems(List<MenuItem> items)
	{
		this.items = items;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((items == null) ? 0 : items.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((params == null) ? 0 : params.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		MenuItem other = (MenuItem) obj;
		if (id == null)
		{
			if (other.id != null) return false;
		}
		else if (!id.equals(other.id)) return false;
		if (items == null)
		{
			if (other.items != null) return false;
		}
		else if (!items.equals(other.items)) return false;
		if (label == null)
		{
			if (other.label != null) return false;
		}
		else if (!label.equals(other.label)) return false;
		if (params == null)
		{
			if (other.params != null) return false;
		}
		else if (!params.equals(other.params)) return false;
		if (type != other.type) return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "MenuItem [type=" + type + ", id=" + id + ", label=" + label + ", params=" + params + ", items=" + items
				+ "]";
	}
}
