package org.molgenis.omx.catalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CatalogPreview
{
	private String title;
	private String description;
	private String version;
	private CatalogPreviewNode root;

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public CatalogPreviewNode getRoot()
	{
		return root;
	}

	public void setRoot(CatalogPreviewNode root)
	{
		this.root = root;
	}

	public static class CatalogPreviewNode
	{
		private String name;
		private List<CatalogPreviewNode> children;
		private List<String> items;

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public List<CatalogPreviewNode> getChildren()
		{
			return children != null ? children : Collections.<CatalogPreviewNode> emptyList();
		}

		public void addChild(CatalogPreviewNode child)
		{
			if (children == null) children = new ArrayList<CatalogPreview.CatalogPreviewNode>();
			children.add(child);
		}

		public List<String> getItems()
		{
			return items != null ? items : Collections.<String> emptyList();
		}

		public void addItem(String item)
		{
			if (items == null) items = new ArrayList<String>();
			items.add(item);
		}
	}
}
