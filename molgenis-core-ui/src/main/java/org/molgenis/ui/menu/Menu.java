package org.molgenis.ui.menu;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Menu extends MenuItem
{
	@SerializedName("items")
	private List<MenuItem> items;

	public List<MenuItem> getItems()
	{
		return items;
	}

	public void setItems(List<MenuItem> items)
	{
		this.items = items;
	}
}
