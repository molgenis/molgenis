package org.molgenis.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TreeNode
{
	private String title;
	private String key;
	private boolean folder;
	private List<TreeNode> children = null;
	private String tooltip;
	private boolean expanded;
	private Map<String, Object> data;

	public TreeNode(String title, String key, String tooltip, boolean folder, boolean expanded, Map<String, Object> data)
	{
		this(title, key, tooltip, folder, expanded, data, null);
	}

	public TreeNode(String title, String key, String tooltip, boolean folder, boolean expanded, Map<String, Object> data, List<TreeNode> children)
	{
		this.title = title;
		this.key = key;
		this.tooltip = tooltip;
		this.folder = folder;
		this.expanded = expanded;
		this.data = data;
		this.children = children;
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
	
	public List<TreeNode> getChildren()
	{
		return children;
	}

	public void addChild(TreeNode child)
	{
		if (children == null)
		{
			children = new ArrayList<TreeNode>();
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

}
