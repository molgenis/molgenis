package org.molgenis.catalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CatalogModelFolder
{
	private String id;
	private String name;
	private boolean selected;
	private List<CatalogModelFolder> children;
	private List<CatalogModelItem> items;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public boolean isSelected()
	{
		return selected;
	}

	public void setSelected(boolean selected)
	{
		this.selected = selected;
	}

	public List<CatalogModelFolder> getChildren()
	{
		return children != null ? children : Collections.<CatalogModelFolder> emptyList();
	}

	public void addChild(CatalogModelFolder child)

	{
		if (this.children == null) children = new ArrayList<CatalogModelFolder>();
		this.children.add(child);
	}

	public List<CatalogModelItem> getItems()
	{
		return items != null ? items : Collections.<CatalogModelItem> emptyList();
	}

	public void addItem(CatalogModelItem item)
	{
		if (this.items == null) items = new ArrayList<CatalogModelItem>();
		this.items.add(item);
	}
}