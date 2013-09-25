package org.molgenis.omx.harmonization.mesh;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class TreeNumberList
{
	List<String> treeNumbers = new ArrayList<String>();

	public List<String> getTreeNumbers()
	{
		return treeNumbers;
	}

	@XmlElement(name = "TreeNumber")
	public void setTreeNumbers(List<String> treeNumberList)
	{
		this.treeNumbers = treeNumberList;
	}
}
