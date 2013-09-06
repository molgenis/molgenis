package org.molgenis.omx.study;

import java.util.List;

public interface StudyDefinitionTreeNode
{
	String getId();

	String getName();

	boolean isSelected();

	List<StudyDefinitionTreeNode> getChildren();

	void addChild(StudyDefinitionTreeNode child);

	List<StudyDefinitionTreeItem> getItems();

	void addItem(StudyDefinitionTreeItem item);

	void sort();
}