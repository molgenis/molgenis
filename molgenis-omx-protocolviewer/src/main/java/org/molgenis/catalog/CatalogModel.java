package org.molgenis.omx.study;

import java.util.Collections;
import java.util.List;

public class StudyDefinitionTree implements StudyDefinitionTreeNode
{
	private String title;
	private String description;
	private String version;
	private List<String> authors;
	private StudyDefinitionTreeNode root;

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

	public List<String> getAuthors()
	{
		return authors;
	}

	public void setAuthors(List<String> authors)
	{
		this.authors = authors;
	}

	public StudyDefinitionTreeNode getRoot()
	{
		return root;
	}

	public void setRoot(StudyDefinitionTreeNode root)
	{
		this.root = root;
	}

	@Override
	public void sort()
	{
		root.sort();
	}

	@Override
	public String getId()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSelected()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<StudyDefinitionTreeNode> getChildren()
	{
		return Collections.singletonList(root);
	}

	@Override
	public void addChild(StudyDefinitionTreeNode child)
	{
		this.root = child;
	}

	@Override
	public List<StudyDefinitionTreeItem> getItems()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void addItem(StudyDefinitionTreeItem item)
	{
		throw new UnsupportedOperationException();
	}
}
