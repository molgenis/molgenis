package org.molgenis.standardsregistry.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PackageTreeNode
{
	private final String title;
	private final String key;
	private final boolean folder;
	private List<PackageTreeNode> children = null;
	private String tooltip;
	private boolean expanded;
	private Map<String, Object> data;
	private final String extraClasses;

	public PackageTreeNode(String extraClasses, String title, String key, String tooltip, boolean folder,
			boolean expanded, Map<String, Object> data)
	{
		this(extraClasses, title, key, tooltip, folder, expanded, data, null);
	}

	public PackageTreeNode(String extraClasses, String title, String key, String tooltip, boolean folder,
			boolean expanded, Map<String, Object> data, List<PackageTreeNode> children)
	{
		this.title = title;
		this.key = key.replace(' ', '_');
		this.tooltip = tooltip;
		this.folder = folder;
		this.expanded = expanded;
		this.data = data;
		this.children = children;
		this.extraClasses = extraClasses;
	}

	public Map<String, Object> getData()
	{
		return data;
	}

	public void setData(Map<String, Object> data)
	{
		this.data = data;
	}

	public boolean isExpanded()
	{
		return expanded;
	}

	public void setExpanded(boolean expanded)
	{
		this.expanded = expanded;
	}

	public String getTitle()
	{
		return title;
	}

	public String getKey()
	{
		return key;
	}

	public boolean isFolder()
	{
		return folder;
	}

	public List<PackageTreeNode> getChildren()
	{
		return children;
	}

	public void addChild(PackageTreeNode child)
	{
		if (children == null)
		{
			children = new ArrayList<PackageTreeNode>();
		}

		children.add(child);
	}

	public String getTooltip()
	{
		return tooltip;
	}

	public void setTooltip(String tooltip)
	{
		this.tooltip = tooltip;
	}

	public String getExtraClasses()
	{
		return extraClasses;
	}

}
