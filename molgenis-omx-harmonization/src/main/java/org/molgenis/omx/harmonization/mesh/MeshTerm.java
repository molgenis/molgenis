package org.molgenis.omx.harmonization.mesh;

import java.util.HashSet;
import java.util.Set;

import org.molgenis.util.SimpleTree;

public class MeshTerm extends SimpleTree<MeshTerm>
{
	private static final long serialVersionUID = 1L;
	private String label = null;
	private String definition = null;
	private Set<String> synonyms = new HashSet<String>();

	public MeshTerm(String name, String label, MeshTerm parent)
	{
		super(name, parent);
		this.label = label;
	}

	public MeshTerm(String name, MeshTerm parent)
	{
		super(name, parent);
	}

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String label)
	{
		this.label = label;
	}

	public Set<String> getSynonyms()
	{
		return synonyms;
	}

	public void setSynonyms(Set<String> synonyms)
	{
		this.synonyms = synonyms;
	}

	public String getPath()
	{
		return name;
	}

	public String getDefinition()
	{
		return definition;
	}

	public void setDefinition(String definition)
	{
		this.definition = definition;
	}
}