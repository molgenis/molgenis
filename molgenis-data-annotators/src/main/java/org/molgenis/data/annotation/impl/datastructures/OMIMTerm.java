package org.molgenis.data.annotation.impl.datastructures;

import java.util.List;

/**
 * Created by jvelde on 2/12/14.
 */
public class OMIMTerm
{

	int entry; // the omim entry nr for this disorder, NOT AN IDENTIFIER
	String name; // name of this disorder, IDENTIFIER
	int type; // 1, 2, 3 or 4, see http://www.omim.org/help/faq section 1.6
	int causedBy; // id of the causing gene/mutation
	String cytoLoc; // cytogenetic location
	List<String> hgncIds; // list of HGNC gene symbols that are associated

	public OMIMTerm(int entry, String name, int type, int causedBy, String cytoLoc, List<String> hgncIds)
	{
		this.entry = entry;
		this.name = name;
		this.type = type;
		this.causedBy = causedBy;
		this.cytoLoc = cytoLoc;
		this.hgncIds = hgncIds;
	}

	public int getEntry()
	{
		return entry;
	}

	public String getName()
	{
		return name;
	}

	public int getType()
	{
		return type;
	}

	public int getCausedBy()
	{
		return causedBy;
	}

	public String getCytoLoc()
	{
		return cytoLoc;
	}

	public List<String> getHgncIds()
	{
		return hgncIds;
	}

	@Override
	public String toString()
	{
		return "OMIMTerm{" + "entry=" + entry + ", name='" + name + '\'' + ", type=" + type + ", causedBy=" + causedBy
				+ ", cytoLoc='" + cytoLoc + '\'' + ", hgncIds=" + hgncIds + '}';
	}
}
