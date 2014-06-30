package org.molgenis.ui.menu;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.google.gson.annotations.SerializedName;

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

	public List<MenuItem> getItems()
	{
		return items;
	}

	public void setItems(List<MenuItem> items)
	{
		this.items = items;
	}
}
