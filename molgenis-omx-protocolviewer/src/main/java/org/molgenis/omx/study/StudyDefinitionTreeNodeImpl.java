package org.molgenis.omx.study;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StudyDefinitionTreeNodeImpl implements StudyDefinitionTreeNode
{
	private final String id;
	private final String name;
	private final boolean selected;
	private List<StudyDefinitionTreeNode> children;
	private List<StudyDefinitionTreeItem> items;

	public StudyDefinitionTreeNodeImpl(String id, String name, boolean selected)
	{
		if (id == null) throw new IllegalArgumentException("id is null");
		if (name == null) throw new IllegalArgumentException("name is null");
		this.id = id;
		this.name = name;
		this.selected = selected;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.omx.study.StudyDefinitionTreeNode#getId()
	 */
	@Override
	public String getId()
	{
		return id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.omx.study.StudyDefinitionTreeNode#getName()
	 */
	@Override
	public String getName()
	{
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.omx.study.StudyDefinitionTreeNode#isSelected()
	 */
	@Override
	public boolean isSelected()
	{
		return selected;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.omx.study.StudyDefinitionTreeNode#getChildren()
	 */
	@Override
	public List<StudyDefinitionTreeNode> getChildren()
	{
		return children != null ? children : Collections.<StudyDefinitionTreeNode> emptyList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.omx.study.StudyDefinitionTreeNode#addChild(org.molgenis.omx.study.StudyDefinitionTreeNode)
	 */
	@Override
	public void addChild(StudyDefinitionTreeNode child)
	{
		if (children == null) children = new ArrayList<StudyDefinitionTreeNode>();
		children.add(child);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.omx.study.StudyDefinitionTreeNode#getItems()
	 */
	@Override
	public List<StudyDefinitionTreeItem> getItems()
	{
		return items != null ? items : Collections.<StudyDefinitionTreeItem> emptyList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.omx.study.StudyDefinitionTreeNode#addItem(org.molgenis.omx.study.StudyDefinitionTreeItem)
	 */
	@Override
	public void addItem(StudyDefinitionTreeItem item)
	{
		if (items == null) items = new ArrayList<StudyDefinitionTreeItem>();
		items.add(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.molgenis.omx.study.StudyDefinitionTreeNode#sort()
	 */
	@Override
	public void sort()
	{
		if (children != null)
		{
			Collections.sort(children, new Comparator<StudyDefinitionTreeNode>()
			{
				@Override
				public int compare(StudyDefinitionTreeNode node1, StudyDefinitionTreeNode node2)
				{
					return node1.getName().compareTo(node2.getName());
				}
			});
			for (StudyDefinitionTreeNode child : children)
			{
				child.sort();
			}
		}
		if (items != null)
		{
			Collections.sort(items, new Comparator<StudyDefinitionTreeItem>()
			{
				@Override
				public int compare(StudyDefinitionTreeItem item1, StudyDefinitionTreeItem item2)
				{
					return item1.getName().compareTo(item2.getName());
				}
			});
		}
	}
}